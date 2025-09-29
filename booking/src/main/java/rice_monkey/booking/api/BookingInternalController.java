package rice_monkey.booking.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rice_monkey.booking.service.BookingService;

@RestController
@RequestMapping("/internal/bookings")
@RequiredArgsConstructor
class BookingInternalController {

    private final BookingService bookingService;

    @PatchMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm(@PathVariable Long id) {
        bookingService.confirm(id);
    }

}
