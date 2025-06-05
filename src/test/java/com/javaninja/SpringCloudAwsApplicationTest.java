package com.javaninja;

import com.javaninja.config.TestAwsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAwsConfig.class)
class SpringCloudAwsApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }
}

