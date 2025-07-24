package rice_monkey.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import testcontainer.config.MySQLTestContainer;

@Testcontainers
@SpringBootTest
class PaymentApplicationTests implements MySQLTestContainer {

	@Test
	void contextLoads() {
	}

}
