package br.com.smartvalidity;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SmartvalidityApplicationTests {

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void contextLoads() {

    }

}
