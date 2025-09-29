package rice_monkey.booking.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "rice_monkey.booking.feign")
public class FeignClientsConfig {
}
