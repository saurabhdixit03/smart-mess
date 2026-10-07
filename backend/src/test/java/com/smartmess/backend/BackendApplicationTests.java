package com.smartmess.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.smartmess.backend.service.EmailService;

@SpringBootTest(properties = {
        "app.seed-demo-data=false",
        "app.billing.automation.enabled=false",
        "app.mail.notifications.enabled=false",
        "app.cashfree.enabled=false"
})
class BackendApplicationTests {

    @MockitoBean
    private EmailService emailService;

    @Test
    void contextLoads() {
    }
}