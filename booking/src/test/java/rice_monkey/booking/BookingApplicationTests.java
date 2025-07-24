package rice_monkey.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import testcontainer.config.MySQLTestContainer;
import testcontainer.config.RedisTestContainer;

@Testcontainers
@SpringBootTest
class BookingApplicationTests implements MySQLTestContainer, RedisTestContainer {

	@Test
	void contextLoads() {
	}

}
