package org.bedrock.teateach.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.web-url:http://localhost:8081}")
    private String webUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TeaTeach - Password Reset Request");
        message.setText("Dear User,\n\n" +
                "You have requested to reset your password for your TeaTeach account. \n\n" +
                "To set a new password, please click on the link below:\n\n" +
                webUrl + "/reset-password?token=" + resetToken + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request a password reset, please ignore this email and your password will remain unchanged.\n\n" +
                "Regards,\n" +
                "The TeaTeach Team");

        mailSender.send(message);
    }
}
