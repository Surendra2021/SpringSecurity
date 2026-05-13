package com.example.SpringJWT.service;

import com.example.SpringJWT.event.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // @EventListener — Spring calls this when UserRegisteredEvent is published
    // @Async — runs in background thread so register() returns immediately
    @Async
    @EventListener
    public void sendWelcomeEmail(UserRegisteredEvent event) {

        // simulating email sending delay
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // in real project — send actual email here
        System.out.println("Welcome email sent to: " + event.getUsername());
    }
}
