package com.aurodining.service.impl;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.aurodining.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * AWS SES Email Service (Production)
 * Sends verification codes via AWS Simple Email Service (SES)
 * 
 * Configuration:
 * - Set email.provider=ses in application.yml
 * - Configure AWS credentials via IAM Role (recommended) or environment variables
 * - Set AWS region (default: us-east-1)
 * - Verify sender email address in AWS SES console
 * 
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "email.provider", havingValue = "ses")
public class AwsSesEmailServiceImpl implements EmailService {

    private final AmazonSimpleEmailService sesClient;
    private final String fromEmail;

    public AwsSesEmailServiceImpl(
            AmazonSimpleEmailService sesClient,
            @Value("${email.from:noreply@aurodining.com}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    @Override
    public boolean sendVerificationCode(String email, String code) {
        try {
            // Validate email format
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                log.error("Invalid email format: {}", email);
                return false;
            }

            // Create email content
            String subject = "Your Auro Dining Verification Code";
            String htmlBody = """
                    <html>
                      <body>
                        <h2>Auro Dining Verification Code</h2>
                        <p>
                          Your verification code is:
                          <strong style="font-size: 24px; color: #ffc200;">%s</strong>
                        </p>
                        <p>This code is valid for 5 minutes.</p>
                        <p>If you didn't request this code, please ignore this email.</p>
                        <hr>
                        <p style="color: #666; font-size: 12px;">
                          Auro Dining - Restaurant Management System
                        </p>
                      </body>
                    </html>
                    """.formatted(code);

            String textBody = """
                    Auro Dining Verification Code

                    Your verification code is: %s

                    This code is valid for 5 minutes.

                    If you didn't request this code, please ignore this email.

                    ---
                    Auro Dining - Restaurant Management System
                    """.formatted(code);

            // Create send request
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(email))
                    .withMessage(new Message()
                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
                            .withBody(new Body()
                                    .withHtml(new Content().withCharset("UTF-8").withData(htmlBody))
                                    .withText(new Content().withCharset("UTF-8").withData(textBody))))
                    .withSource(fromEmail);

            // Send email
            SendEmailResult result = sesClient.sendEmail(request);
            log.info("Email sent successfully. Email: {}, MessageId: {}", email, result.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("Failed to send email via AWS SES. Email: {}", email, e);
            return false;
        }
    }
}
