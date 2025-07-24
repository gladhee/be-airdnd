package rice_monkey.listing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import testcontainer.config.MySQLTestContainer;

@Testcontainers
@SpringBootTest
class ListingApplicationTests implements MySQLTestContainer {

	@Test
	void contextLoads() {
	}

}
