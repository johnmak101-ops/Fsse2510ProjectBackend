package com.fsse2510.fsse2510_project_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "firebase.enabled=false",
        "app.frontend.url=http://localhost:3000",
        "stripe.secret.key=test_sk",
        "stripe.webhook.secret=test_whsec",
        "app.admin.emails=admin@test.com",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class Fsse2510ProjectBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
