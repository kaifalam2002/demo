package com.example.demo;

import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
@Service
public class WhatsappTemplateSender {

    private static final String ACCOUNT_SID = System.getenv("sid");
    private static final String AUTH_TOKEN = System.getenv("auth_token");
    private static final String FROM_NUMBER = "whatsapp:+14155238886"; // Sandbox number

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public String sendCustomMessage(String to, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber("whatsapp:+" + to),
                new PhoneNumber(FROM_NUMBER),
                messageBody
        ).create();

        return message.getSid();
    }
}

