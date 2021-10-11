package com.fielden.christmas_list;

public class CreateEmail {
    static String createEmail(String usernameFrom, String listTitle) {
        return "<h1>Gift and present ideas</h1>" +
                "<br>" +
                "<br>" +
                 usernameFrom + " has invited you view their list of gift ideas: <b>" + listTitle + "</b>" +
                "<br>" +
                "<br>" +
                "Go to <a href=\"http:localhost:8080\">www.giftandpresentideas.com</a> to view the list";
    }
}
