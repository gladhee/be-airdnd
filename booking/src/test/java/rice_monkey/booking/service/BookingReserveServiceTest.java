package rice_monkey.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.client.RedisBusyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class BookingReserveServiceTest implements MySQLTestContainer, RedisTestContainer {

    @Autowired
    private BookingReserveService bookingReserveService;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private ListingClient listingClient;

    @BeforeEach
    void clear() {
        bookingRepository.deleteAll();
        ListingDto mockListing = new ListingDto(
                1L, "테스트 숙소", 10000, "REQUESTED"
        );
        Mockito.when(listingClient.find(Mockito.anyLong()))
                .thenReturn(mockListing);
    }

    @DisplayName("[동시에 100개의 예약 요청이 들어오면 단 하나만 성공해야 한다]")
    @Test
    void 예약_동시성_테스트() throws InterruptedException {
        // given
        LocalDate checkin = LocalDate.of(2025, 8, 5);
        LocalDate checkout = LocalDate.of(2025, 8, 6);
        long listingId = 12345L;
        long userId = 1L;

        BookingReserveRequestDto request = new BookingReserveRequestDto(
                listingId, checkin, checkout, 2
        );

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // --- 1) 여기서 모두 대기 ---
                    startLatch.await();

                    // --- 2) 동시에 예약 시도 ---
                    bookingReserveService.reserve(request, userId);
                } catch (AlreadyBookedException | RedisBusyException e) {
                    // 이미 예약됐거나 락 실패인 경우 무시
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    // --- 3) 종료 래치 카운트 다운 ---
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        // then
        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings)
                .as("최종적으로 단 하나의 예약만 성공해야 한다")
                .hasSize(1);
    }

}
