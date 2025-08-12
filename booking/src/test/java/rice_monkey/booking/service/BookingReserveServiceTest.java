package rice_monkey.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.client.RedisBusyException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import rice_monkey.booking.dao.BookingRepository;
import rice_monkey.booking.domain.Booking;
import rice_monkey.booking.dto.request.BookingReserveRequestDto;
import rice_monkey.booking.exception.business.booking.AlreadyBookedException;
import rice_monkey.booking.feign.listing.ListingClient;
import rice_monkey.booking.feign.listing.dto.ListingDto;
import testcontainer.config.MySQLTestContainer;
import testcontainer.config.RedisTestContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class BookingReserveServiceTest implements MySQLTestContainer, RedisTestContainer {

    @Autowired
    private BookingReserveService bookingReserveService;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private ListingClient listingClient;

    @BeforeEach
    void clear() {
        bookingRepository.deleteAll();

        // 외부 의존성 제거: Listing 서비스 Mock
        ListingDto mockListing = new ListingDto(
                1L, "테스트 숙소", 10000, "REQUESTED"
        );
        Mockito.when(listingClient.find(Mockito.anyLong()))
                .thenReturn(mockListing);
    }

    /**
     * 모든 워커가 준비(ready) 후 동시에(start) 출발하도록 보장하고,
     * 예외는 Future.get()으로 끌어올려 테스트를 실패시킵니다.
     */
    private void runSimultaneously(int threadCount,
                                   IntFunction<Callable<Void>> taskFactory) throws Exception {
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Future<Void>> futures = new ArrayList<>(threadCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                Callable<Void> worker = () -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start signal timeout");
                    }
                    try {
                        return taskFactory.apply(idx).call();
                    } finally {
                        done.countDown();
                    }
                };
                futures.add(executor.submit(worker));
            }

            // 모든 워커가 준비됐는지 확인 후 동시에 출발
            if (!ready.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Workers not ready in time");
            }
            start.countDown();

            // 무한대기 방지
            if (!done.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Workers didn't finish in time");
            }

            // 워커 내부의 예외를 표면화
            for (Future<Void> f : futures) {
                f.get(); // 예외 발생 시 여기서 던져져 테스트 실패
            }
        }
    }

    private static boolean isExpectedConcurrencyFailure(Throwable e) {
        return e instanceof AlreadyBookedException
                || e instanceof RedisBusyException
                || e instanceof RedisTimeoutException;
    }

    private void tryReserve(BookingReserveRequestDto req, long guestId, AtomicInteger successCount) {
        try {
            bookingReserveService.reserve(req, guestId);
            successCount.incrementAndGet();
        } catch (Throwable e) {
            // 동시성 경합 시 예상 가능한 실패는 무시
            if (!isExpectedConcurrencyFailure(e)) {
                // 예상 밖의 예외는 테스트 실패로 올림
                throw e;
            }
        }
    }

    @DisplayName("[시나리오 1: 동일 날짜 동시 예약] 100개의 요청이 동시에 들어오면 단 하나만 성공해야 한다.")
    @Test
    void testConcurrentBooking_SameDates() throws Exception {
        // given
        final int threadCount = 100;
        final long listingId = 101L;
        final long guestId = 1L;
        final LocalDate checkin = LocalDate.of(2025, 8, 10);
        final LocalDate checkout = LocalDate.of(2025, 8, 15);

        BookingReserveRequestDto request = new BookingReserveRequestDto(listingId, checkin, checkout, 2);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        runSimultaneously(threadCount, i -> () -> {
            tryReserve(request, guestId, successCount);
            return null;
        });

        // then
        assertThat(successCount.get()).isEqualTo(1);
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
    }

    @DisplayName("[시나리오 2: 겹치는 날짜 동시 예약] 겹치는 날짜에 대한 동시 요청은 하나만 성공해야 한다.")
    @Test
    void testConcurrentBooking_OverlappingDates() throws Exception {
        // given
        final int threadCount = 2;
        final long listingId = 202L;

        BookingReserveRequestDto request1 = new BookingReserveRequestDto(
                listingId, LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 12), 2);
        BookingReserveRequestDto request2 = new BookingReserveRequestDto(
                listingId, LocalDate.of(2025, 8, 11), LocalDate.of(2025, 8, 13), 2);

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        runSimultaneously(threadCount, idx -> () -> {
            if (idx % 2 == 0) {
                tryReserve(request1, 1L, successCount);
            } else {
                tryReserve(request2, 2L, successCount);
            }
            return null;
        });

        // then
        // 겹치는 날짜에 대한 동시 요청은 하나만 성공해야 한다.
        assertThat(successCount.get()).isEqualTo(1);
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
    }

    @DisplayName("[시나리오 3: 겹치지 않는 날짜 동시 예약] 겹치지 않는 날짜에 대한 동시 요청은 모두 성공해야 한다.")
    @Test
    void testConcurrentBooking_NonOverlappingDates() throws Exception {
        // given
        final int threadCount = 2;
        final long listingId = 303L;

        BookingReserveRequestDto request1 = new BookingReserveRequestDto(
                listingId, LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 12), 2);
        BookingReserveRequestDto request2 = new BookingReserveRequestDto(
                listingId, LocalDate.of(2025, 8, 13), LocalDate.of(2025, 8, 15), 2);

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        runSimultaneously(threadCount, idx -> () -> {
            if (idx % 2 == 0) {
                tryReserve(request1, 1L, successCount);
            } else {
                tryReserve(request2, 2L, successCount);
            }
            return null;
        });

        // then
        assertThat(successCount.get()).isEqualTo(2);
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(2);
    }

    @DisplayName("[시나리오 4: 다른 숙소 동시 예약] 다른 숙소에 대한 동시 요청은 모두 성공해야 한다.")
    @Test
    void testConcurrentBooking_DifferentListings() throws Exception {
        // given
        final int threadCount = 2;
        final LocalDate checkin = LocalDate.of(2025, 8, 10);
        final LocalDate checkout = LocalDate.of(2025, 8, 15);

        // 서로 다른 숙소
        BookingReserveRequestDto request1 = new BookingReserveRequestDto(410L, checkin, checkout, 2);
        BookingReserveRequestDto request2 = new BookingReserveRequestDto(420L, checkin, checkout, 2);

        AtomicInteger successCount = new AtomicInteger(0);

        // when
        runSimultaneously(threadCount, idx -> () -> {
            if (idx % 2 == 0) {
                tryReserve(request1, 1L, successCount);
            } else {
                tryReserve(request2, 2L, successCount);
            }
            return null;
        });

        // then
        assertThat(successCount.get()).isEqualTo(2);
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(2);
    }

}
