package com.pfa.pfaproject.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailRequest {
    // Getters et Setters
    private String from;
    private String to;
    private String subject;
    private String text;
    private String name;

    // Constructeurs
    public EmailRequest() {
    }

    public EmailRequest(String from, String to, String subject, String text, String name) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.name = name;
    }

}
