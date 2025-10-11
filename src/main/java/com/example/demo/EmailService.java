package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Send plain text email
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kaifalam12a@gmail.com");
        message.setTo(to);
        message.setCc("kaifalamnazneen@gmail.com");
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    // Send email with PDF attachment
    public void sendEmailWithAttachment(String to, String subject, String text, byte[] pdfBytes, String filename)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("kaifalam12a@gmail.com");
        helper.setTo(to);
        helper.setCc("mdsaifalam0067@gmail.com");
        helper.setSubject(subject);
        helper.setText(text, true); // second parameter 'true' tells it to treat text as HTML

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        helper.addAttachment(filename, resource);

        mailSender.send(mimeMessage);
    }
}

