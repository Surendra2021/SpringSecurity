package com.example.SpringJWT.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // @Async — Spring runs this method in a separate background thread
    // the caller does not wait for this to finish
    @Async
    public void sendWelcomeEmail(String username) {

        // simulating email sending delay — 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // in real project — you'd use JavaMailSender here to send actual email
        System.out.println("Welcome email sent to: " + username);
    }
}
