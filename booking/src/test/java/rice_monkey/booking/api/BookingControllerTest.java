package rice_monkey.booking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.client.RedisBusyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rice_monkey.booking.dto.response.BookingReserveResponseDto;
import rice_monkey.booking.dto.response.BookingResponseDto;
import rice_monkey.booking.exception.business.booking.BookingNotFoundException;
import rice_monkey.booking.service.BookingReserveService;
import rice_monkey.booking.service.BookingService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean BookingService bookingService;
    @MockitoBean BookingReserveService bookingReserveService;

    @DisplayName("GET 예약조회 성공 → 200 OK")
    @Test
    void getBookingSuccess() throws Exception {
        // given
        var dto = mock(BookingResponseDto.class);
        when(bookingService.getBooking(anyLong(), anyLong())).thenReturn(dto);

        // when & then
        mvc.perform(get("/api/bookings/{id}", 1L)
                        .header("X-User-Id", 100L))
                .andExpect(status().isOk());

        verify(bookingService).getBooking(anyLong(), anyLong());
    }

    @DisplayName("POST 예약요청 성공 → 201 Created")
    @Test
    void reserveBookingSuccess() throws Exception {
        // given
        var body = Map.of(
                "listingId", 11,
                "checkin", "2025-09-01",
                "checkout", "2025-09-03",
                "guestCount", 2
        );
        var resp = new BookingReserveResponseDto(1L, "330000", 111133);
        when(bookingReserveService.reserve(any(), eq(7L))).thenReturn(resp);

        // when & then
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Id", 7L))
                .andExpect(status().isCreated());

        verify(bookingReserveService).reserve(any(), eq(7L));
    }

    @DisplayName("DELETE 예약취소 성공 → 204 No Content")
    @Test
    void cancelBookingSuccess() throws Exception {
        // given

        // when & then
        mvc.perform(delete("/api/bookings/{id}", 3L)
                        .header("X-User-Id", 7L))
                .andExpect(status().isNoContent());

        verify(bookingService).cancelBooking(3L, 7L);
    }

    /**
     * @todo X-User-Id 헤더 검증 Spring Security Filter 에서 처리하도록 변경 후 테스트 케이스 수정
     * */
    @DisplayName("X-User-Id 헤더 누락 → 500 Internal Server Error")
    @Test
    void missingHeaderBadRequest() throws Exception {
        // given

        // when & then
        mvc.perform(get("/api/bookings/{id}", 1L))
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("X-User-Id 헤더 형식 오류 (문자열 → Long 변환 실패) → 500 Internal Server Error")
    @Test
    void invalidHeaderTypeBadRequest() throws Exception {
        // given

        // when & then
        mvc.perform(get("/api/bookings/{id}", 1L)
                        .header("X-User-Id", "abc"))
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("guestCount 음수 → 400 Invalid Request")
    @Test
    void invalidGuestCountValidationError() throws Exception {
        // given
        var body = Map.of(
                "listingId", 11,
                "checkin", "2025-09-01",
                "checkout", "2025-09-03",
                "guestCount", -1
        );

        // when & then
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Id", 7L))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("날짜 포맷 오류 → 400 Bad Request")
    @Test
    void invalidDateFormatBadRequest() throws Exception {
        // given
        var body = Map.of(
                "listingId", 11,
                "checkin", "09-01-2025", // 잘못된 포맷
                "checkout", "2025-09-03",
                "guestCount", 2
        );

        // when & then
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Id", 7L))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("예약 없음 → BusinessException 404 Not Found")
    @Test
    void bookingNotFoundBusinessException() throws Exception {
        // given
        long bookingId = 99;
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new BookingNotFoundException(bookingId));

        // when & then
        mvc.perform(get("/api/bookings/{id}", 99L)
                        .header("X-User-Id", 7L))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Redis Busy → 429 Too Many Requests")
    @Test
    void redisBusyException() throws Exception {
        // given
        when(bookingReserveService.reserve(any(), eq(7L)))
                .thenThrow(new RedisBusyException("Too many concurrent reservations"));

        var body = Map.of(
                "listingId", 11,
                "checkin", "2025-09-01",
                "checkout", "2025-09-03",
                "guestCount", 2
        );

        // when & then
        mvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .header("X-User-Id", 7L))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "2"));
    }

    @DisplayName("알 수 없는 서버 에러 → 500 General Error")
    @Test
    void generalException500() throws Exception {
        // given
        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Unexpected"));

        // when & then
        mvc.perform(get("/api/bookings/{id}", 1L)
                        .header("X-User-Id", 7L))
                .andExpect(status().isInternalServerError());
    }

}
