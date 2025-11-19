package com.phuc.notification.service;

import com.phuc.notification.dto.request.SendEmailRequest;
import com.phuc.notification.dto.response.EmailResponse;


public interface EmailService {

    EmailResponse sendEmail(SendEmailRequest request);

}
