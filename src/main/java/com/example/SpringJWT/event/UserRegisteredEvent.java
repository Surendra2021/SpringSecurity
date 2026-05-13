package com.example.SpringJWT.event;

// This is the event object — just carries data about what happened
// No logic here — just a message passed between classes
public class UserRegisteredEvent {

    // the username of the user who just registered
    private final String username;

    // constructor — created when event is published
    public UserRegisteredEvent(String username) {
        this.username = username;
    }

    // getter — listeners use this to get the username
    public String getUsername() {
        return username;
    }
}
