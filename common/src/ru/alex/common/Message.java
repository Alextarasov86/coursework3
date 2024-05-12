package ru.alex.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String sender;
    private String text;
    private LocalDateTime sentAt;

    public Message(String sender) {
        this.sender = sender;
    }

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }
}
