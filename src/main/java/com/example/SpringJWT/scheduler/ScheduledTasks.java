package com.example.SpringJWT.scheduler;

import com.example.SpringJWT.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// @Component — registers this class as a Spring bean
// so Spring can manage and call @Scheduled methods automatically
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final UserRepository userRepository;

    // fixedRate = 60000 — runs every 60 seconds automatically
    // Spring calls this method — no one triggers it manually
    @Scheduled(fixedRate = 60000)
    public void reportUserCount() {
        long count = userRepository.count();
        System.out.println("Total registered users: " + count);
    }

    // cron = "0 0 * * * *" — runs every hour at minute 0
    // cron format: second minute hour day month weekday
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyCheck() {
        System.out.println("Hourly check — app is running fine");
    }
}
