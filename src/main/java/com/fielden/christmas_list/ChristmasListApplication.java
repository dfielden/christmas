package com.fielden.christmas_list;

import com.fielden.christmas_list.auth.Login;
import com.fielden.christmas_list.auth.PasswordSecurity;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@Controller
public class ChristmasListApplication {
    private final ListDB db;

    @Autowired
    private JavaMailSender mailSender;

    private static final Gson gson = new Gson();
    private static final Random rand = new Random();
    private final Map<String, ListAppState> sessions = new ConcurrentHashMap<>(); // cookieValue, ListAppState
    private static final String LIST_COOKIE_NAME = "LISTAPPCOOKIE";
    public static final String LOGIN_SUCCESS_RESPONSE_VALUE = "LOGIN_SUCCESS";
    public static final String SIGNUP_SUCCESS_RESPONSE_VALUE = "SIGNUP_SUCCESS";
    public static final String LOGOUT_SUCCESS_RESPONSE_VALUE = "LOGOUT_SUCCESS";

    public ChristmasListApplication() throws Exception {
        db = new ListDB();
    }

    public static void main(String[] args) {
        SpringApplication.run(ChristmasListApplication.class, args);
    }

    @GetMapping("/")
    public String index(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        if (!state.isLoggedIn()) {
            return "redirect:/login";
        }
        return "index";
    }

    @ResponseBody
    @GetMapping("/api/mylists")
    public HashMap<Integer, ListHeader> getMyLists(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.getMyLists(userId);
    }


    @GetMapping("/mylist/{id}")
    public String myList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        if (!state.isLoggedIn()) {
            return "redirect:/login";
        }
        return "mylist";
    }

    @ResponseBody
    @GetMapping("/api/list/{id}")
    public HashMap<Integer, ItemInList> getListFromId(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.getListAndHideSelectedStatus(listId, userId);
    }

    @ResponseBody
    @PostMapping("/list/new")
    public int createNewList(HttpServletRequest req, HttpServletResponse resp, @RequestBody String listTitle) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.createList(userId, listTitle.substring(1, listTitle.length()-1));
    }

    @ResponseBody
    @PostMapping("/add/{id}")
    public int addToList(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp, @RequestBody ItemInList item) throws Exception {
        System.out.println(item);
        return db.addItem(listId, item);
    }

    @GetMapping("/signup")
    public String getSignup(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        if (state.isLoggedIn()) {
            return "redirect:/";
        }
        return "signup";
    }

    @ResponseBody
    @PostMapping(value="/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String signup(@RequestBody Login login, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String email = login.getEmail();
            String username = login.getUsername();
            String typedPassword = login.getPassword();
            String[] hashedPassword = PasswordSecurity.createHashedPassword(typedPassword);

            db.signup(email, username, hashedPassword[0], hashedPassword[1]);
        } catch (IllegalStateException e) {
            return gson.toJson(e.getMessage());
        }
        return gson.toJson(SIGNUP_SUCCESS_RESPONSE_VALUE);
    }

    @ResponseBody
    @GetMapping(value="/title/{id}")
    public String signup(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return gson.toJson(db.getTitle(listId, userId));
    }

    @ResponseBody
    @PostMapping(value="/edit/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String editEntry(@PathVariable(value="id") int itemId, @RequestBody ItemInList item, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        db.updateItem(itemId, item);
        return gson.toJson(item);
    }

    @ResponseBody
    @PostMapping(value="/email/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String addEmailAddress(@PathVariable(value="id") int listId, @RequestBody String emails, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        System.out.println(emails);
        db.updateEmails(listId, emails);
        return gson.toJson(emails);
    }

    @ResponseBody
    @GetMapping("/api/emails/{id}")
    public String getListEmails(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.getSharedEmails(listId);
    }

    @ResponseBody
    @PostMapping("/message")
    public String postMessage(@RequestBody Message message, HttpServletRequest req, HttpServletResponse resp) throws Exception {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper email = new MimeMessageHelper(mimeMessage);
        email.setFrom(message.getEmail());
        email.setTo("happyfaceenterprises@gmail.com");
        email.setSubject("NEW NAUTICAL WHEEL QUERY");
        email.setText(CreateEmail.createEmail(message), true);

        mailSender.send(mimeMessage);

        return gson.toJson("hello");
    }

    @GetMapping("/login")
    public String getLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        if (state.isLoggedIn()) {
            return "redirect:/";
        }
        return "login";
    }

    @ResponseBody
    @PostMapping(value="/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(@RequestBody Login login, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String email = login.getEmail();
            String enteredPassword = login.getPassword();

            HashMap<String, String> userDetails = db.getuserDetailsFromEmail(email);
            String salt = userDetails.get("salt");
            String hashedPassword = userDetails.get("hashedPassword");

            if (hashedPassword.equals(PasswordSecurity.hashString(enteredPassword + salt))) {
                // User credentials OK.

                ListAppState state = getOrCreateSession(req, resp);
                state.setUserName(userDetails.get("userName"));
                state.setUserId(Integer.parseInt(userDetails.get("userId")));
                return gson.toJson(LOGIN_SUCCESS_RESPONSE_VALUE);
            } else {
                // User credentials BAD.
                return gson.toJson("Incorrect password. Please try again.");
            }
        } catch (IllegalStateException e) {
            return gson.toJson(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Cookie c = findOrSetSessionCookie(req, resp);
        c.setMaxAge(0);
        resp.addCookie(c);
        HttpSession session = req.getSession();
        session.invalidate();
        req.logout();
        return "login";
    }

    @Nonnull
    private synchronized ListAppState getOrCreateSession(HttpServletRequest req, HttpServletResponse resp) {
        // First, get the Cookie from the request.
        Cookie cookie = findOrSetSessionCookie(req, resp);

        // Use the cookie value as the session ID.
        String sessionId = cookie.getValue();

        // Then, look up the corresponding session for this Cookie ID.
        ListAppState state = sessions.get(sessionId);

        if (state == null) {
            // Create a new session (findOrSetSessionCookie probably just created the Cookie, so there is not yet a
            // corresponding session).
            state = new ListAppState();
            sessions.put(sessionId, state);
        }

        return state;
    }

    @Nonnull
    private static Cookie findOrSetSessionCookie(HttpServletRequest req, HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (LIST_COOKIE_NAME.equals(c.getName())) {
                    // Found our cookie.
                    return c;
                }
            }
        }

        // No cookie. Set a new one.
        Cookie cookie = new Cookie(LIST_COOKIE_NAME, String.format("%x%xlist", rand.nextLong(), rand.nextLong()));
        resp.addCookie(cookie);
        return cookie;
    }

    private static final class ListAppState {
        private int userId = -1;
        @Nullable  // If logged out, this is null.
        private String userName;

        boolean isLoggedIn() {
            return this.userId > 0;
        }

        @Nullable
        public String getUserName() {
            return this.userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getUserId() {
            return this.userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "id: " + this.getUserId() + ", user name: " + this.getUserName();
        }

    }
}
