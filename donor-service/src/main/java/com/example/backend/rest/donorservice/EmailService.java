package com.example.backend.rest.donorservice;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDonationConfirmation(String toEmail, BloodRequestDto request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Blood Donation Registration Confirmation");
            message.setText(createDonationConfirmationText(request));

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String createDonationConfirmationText(BloodRequestDto request) {
        StringBuilder text = new StringBuilder();
        text.append("Dear Donor,\n\n")
                .append("Thank you for registering for blood donation! ⸜(｡˃ ᵕ ˂ )⸝♡\n\n")
                .append(" ( ദ്ദി ˙ᗜ˙ ) Your registration has been successfully received.\n\n")
                .append("A medical center representative will contact you shortly to confirm your donation appointment.\n\n")
                .append("Please ensure you meet the standard donor eligibility requirements and bring valid identification.\n\n")
                .append("Thank you for your willingness to save lives.\n\n")
                .append("Sincerely,\n")
                .append("BloodConnect Team (っ˶ ˘ ᵕ˘)ˆᵕ ˆ˶ς) ");

        return text.toString();
    }
}