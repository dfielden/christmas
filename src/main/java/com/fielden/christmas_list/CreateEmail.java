package com.fielden.christmas_list;

public class CreateEmail {
    static String createEmail(String usernameFrom, String listTitle) {
        return "<h1>Gift and present ideas</h1>" +
                "<br>" +
                "<br>" +
                 usernameFrom + " has invited you view their list of gift ideas: <b>" + listTitle + "</b>" +
                "<br>" +
                "<br>" +
                "Go to <a href=\"giftandpresentideas.com\">giftandpresentideas.com</a> to view the list." +
                "<br>" +
                "<br>" +
                "If the URL does not work, please try copying and pasting it into your browser. If this still does not work, please contact Dan (dan@danfielden.com) for help.";
    }

    static String createEmailGuest(String usernameFrom, String listTitle, String email) {
        return "<h1>Gift and present ideas</h1>" +
                "<br>" +
                "<br>" +
                usernameFrom + " has invited you view their list of gift ideas: <b>" + listTitle + "</b>" +
                "<br>" +
                "<br>" +
                "Go to <a href=\"giftandpresentideas.com\">giftandpresentideas.com</a> to view the list." +
                "<br>" +
                "<br>" +
                "Please use the email address to which this email was send as your username when signing up (" + email + ")." +
                "<br>" +
                "<br>" +
                "If the URL does not work, please try copying and pasting it into your browser. If this still does not work, please contact Dan (dan@danfielden.com) for help.";
    }
}
