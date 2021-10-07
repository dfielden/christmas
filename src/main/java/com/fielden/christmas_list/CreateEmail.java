package com.fielden.christmas_list;

public class CreateEmail {
    static String createEmail(Message message) {
        return "<h1>New message</h1>" +
                "<br>" +
                "<br>" +
                "<b>From:</b>" + " " + message.getName() +
                "<br>" +
                "<b>Email:</b>" + " " + message.getEmail() +
                "<br>" +
                "<b>Subject:</b>" + " " + message.getSubject() +
                "<br>" +
                "<b>Message:</b>" + " " + message.getMessage();

    }
}
