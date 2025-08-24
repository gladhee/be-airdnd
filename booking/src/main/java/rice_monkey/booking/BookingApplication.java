package rice_monkey.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.ZoneOffset;
import java.util.TimeZone;

@SpringBootApplication
public class BookingApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        SpringApplication.run(BookingApplication.class, args);
    }

}
