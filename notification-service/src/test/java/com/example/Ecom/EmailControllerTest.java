package com.example.Ecom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phuc.notification.dto.request.Recipient;
import com.phuc.notification.dto.request.SendEmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.phuc.notification.NotificationServiceApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureWebMvc
class EmailControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSendEmail() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test email request
        SendEmailRequest request = SendEmailRequest.builder()
                .to(Recipient.builder()
                        .name("Test User")
                        .email("test@example.com")
                        .build())
                .subject("Test Email")
                .htmlContent("<h1>Hello World!</h1><p>This is a test email.</p>")
                .build();

        // Perform POST request
        mockMvc.perform(post("/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.messageId").exists());
    }

    @Test
    void testSendEmailWithInvalidData() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create invalid email request (missing required fields)
        SendEmailRequest request = SendEmailRequest.builder()
                .to(Recipient.builder()
                        .name("Test User")
                        .email("") // Empty email
                        .build())
                .subject("") // Empty subject
                .htmlContent("") // Empty content
                .build();

        // Perform POST request
        mockMvc.perform(post("/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
