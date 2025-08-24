package rice_monkey.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record BookingReserveRequestDto(
        @NotNull @Positive Long listingId,
        @NotNull LocalDate checkin,
        @NotNull LocalDate checkout,
        @NotNull @Positive Integer guestCount
) {
}
