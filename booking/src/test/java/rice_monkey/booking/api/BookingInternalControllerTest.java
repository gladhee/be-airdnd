package rice_monkey.booking.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rice_monkey.booking.exception.business.booking.BookingNotFoundException;
import rice_monkey.booking.service.BookingService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingInternalController.class)
class BookingInternalControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BookingService bookingService;

    @DisplayName("PATCH 예약 확정 요청 성공 -> 200 OK")
    @Test
    void confirmBookingSuccess() throws Exception {
        // when & then
        mockMvc.perform(patch("/internal/bookings/{id}/confirm", 1L))
                .andExpect(status().isNoContent());

        verify(bookingService).confirm(1L);
    }

    @DisplayName("PATCH 예약 확정 요청 실패 - 존재하지 않는 예약")
    @Test
    void confirmBookingFail_NotFound() throws Exception {
        // given
        doThrow(new BookingNotFoundException(1L))
                .when(bookingService).confirm(1L);

        // when & then
        mockMvc.perform(patch("/internal/bookings/{id}/confirm", 1L))
                .andExpect(status().isNotFound());

        verify(bookingService).confirm(1L);
    }

}
