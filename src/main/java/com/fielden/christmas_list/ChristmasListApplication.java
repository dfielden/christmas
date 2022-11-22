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
    private final HashMap<String, String> users = new HashMap<>();
    private static final String LIST_COOKIE_NAME = "LISTAPPCOOKIE";
    public static final String LOGIN_SUCCESS_RESPONSE_VALUE = "LOGIN_SUCCESS";
    public static final String SIGNUP_SUCCESS_RESPONSE_VALUE = "SIGNUP_SUCCESS";
    public static final String LOGOUT_SUCCESS_RESPONSE_VALUE = "LOGOUT_SUCCESS";

    public ChristmasListApplication() throws Exception {
        users.put("Dan", "dan@danfielden.com");
        users.put("James", "james@jgubby.com");
        users.put("Jan", "janfielden38@gmail.com");
        users.put("Julia", "juliaafielden@gmail.com");
        users.put("Madelaine", "madelaine.fielden@gmail.com");
        users.put("Maddy", "maddyj4.x@gmail.com");
        users.put("Mark", "markf.mobile25@googlemail.com");

        db = new ListDB(users);
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

    @ResponseBody
    @GetMapping("/api/sharedlists")
    public HashMap<Integer, ListHeaderShared> getSharedLists(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        String myEmail = state.getEmail();
        return db.getSharedLists(myEmail);
    }


    @GetMapping("/mylist/{id}")
    public String myList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        System.out.println(state);
        if (!state.isLoggedIn()) {
            System.out.println("not logged in");
            return "redirect:/login";
        }
        return "mylist";
    }

    @GetMapping("/sharedlist/{id}")
    public String sharedList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        if (!state.isLoggedIn()) {
            return "redirect:/login";
        }
        return "sharedlist";
    }

    @ResponseBody
    @GetMapping("/api/mylist/{id}")
    public HashMap<Integer, ItemInList> getMyListFromId(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        System.out.println(state);
        int userId = state.getUserId();
        if (!db.checkUserOwnsList(listId, userId)) {
            throw new IllegalStateException("User does not own list");
        }
        return db.getListAndHideSelectedStatus(listId, userId);
    }

    @ResponseBody
    @GetMapping("/api/sharedlist/{id}")
    public HashMap<Integer, ItemInList> getSharedListFromId(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        if (!db.checkUserCanViewList(listId, userId)) {
            throw new IllegalStateException("User does not have permission to view list");
        }
        return db.getList(listId, userId);
    }

    @ResponseBody
    @GetMapping("/api/customcontacts")
    public ArrayList<String> getCustomContacts(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.getCustomContacts(userId);

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
            String username = login.getUsername();
            String email = db.getEmailFromUsernameSignup(username);
            String typedPassword = login.getPassword();
            String[] hashedPassword = PasswordSecurity.createHashedPassword(typedPassword);

            db.signup(email, username, hashedPassword[0], hashedPassword[1]);
        } catch (IllegalStateException e) {
            return gson.toJson(e.getMessage());
        }
        return gson.toJson(SIGNUP_SUCCESS_RESPONSE_VALUE);
    }

    @ResponseBody
    @GetMapping(value="/api/title/{id}")
    public String signup(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return gson.toJson(db.getTitle(listId, userId));
    }

    @ResponseBody
    @GetMapping(value="/api/username/{id}")
    public String getUserName(@PathVariable(value="id") int userId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return gson.toJson(db.getUsername(userId));
    }

    @ResponseBody
    @GetMapping(value="/api/ownusername")
    public String getOwnUserName(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return gson.toJson(db.getUsername(userId));
    }

    @ResponseBody
    @GetMapping(value="/api/myuserid")
    public String getMyUserId(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        return gson.toJson(state.getUserId());
    }

    @ResponseBody
    @GetMapping(value="/api/selectitem/{id}")
    public boolean selectItem(@PathVariable(value="id") int itemId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        return db.selectItem(itemId, userId);
    }

    @ResponseBody
    @GetMapping(value="/api/deselectitem/{id}")
    public String deselectItem(@PathVariable(value="id") int itemId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        db.deselectItem(itemId);
        return gson.toJson(itemId);
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
    public String addEmailAddress(@PathVariable(value="id") int listId, @RequestBody String contact, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ListAppState state = getOrCreateSession(req, resp);
        String emailAddress = db.getEmailFromUsernameSignup(contact.substring(1, contact.length()-1));

        if (emailAddress.equals(state.getEmail())) {
            throw new IllegalArgumentException("You cannot share your list with yourself");
        }

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        db.addSharedWith(listId, emailAddress);
        return gson.toJson(emailAddress);
    }

    @ResponseBody
    @PostMapping(value="/deleteemail/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteEmailAddress(@PathVariable(value="id") int listId, @RequestBody String contact, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        String emailAddress = db.getEmailFromUsernameSignup(contact.substring(1, contact.length() - 1));
        db.deleteSharedEmail(listId, emailAddress);
        return gson.toJson(emailAddress);
    }

    @ResponseBody
    @PostMapping(value="/newuser",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String addEmailAddress(@RequestBody String email, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        int newUserId = db.addUser(email.substring(1, email.length()-1), email.substring(1, email.length()-1), false); // returns either id of existing user (if previously added, or id of newly added user)
        db.addCustomShare(userId, newUserId, email.substring(1, email.length()-1));
        return gson.toJson(email);
    }

    @ResponseBody
    @GetMapping("/api/emails/{id}")
    public String getListContacts(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return gson.toJson(db.getSharedContacts(listId));
    }

    @ResponseBody
    @PostMapping("/sharelist/{id}")
    public HashMap<String, Boolean> postMessage(@PathVariable(value="id") int listId, @RequestBody ArrayList<String> contacts, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HashMap<String, Boolean> sentStatus = new HashMap<>();
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        ListAppState state = getOrCreateSession(req, resp);
        int userId = state.getUserId();
        String usernameFrom = db.getUsername(userId);
        String listTitle = db.getTitle(listId, userId);
        for (String contact : contacts) {
            String email = db.getEmailFromUsernameSignup(contact);
            if(db.checkEmailSent(email, listId) || sendEmail(email, usernameFrom, listTitle, userId, listId)) {
                sentStatus.put(email, true);
                db.updateEmailSent(email, listId);
            } else {
                sentStatus.put(email, false);
            }
        }
        db.setListAsShared(listId);
        return sentStatus;
    }

    @ResponseBody
    @GetMapping("/api/listsharedstatus/{id}")
    public boolean isListShared(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return db.checkListIsShared(listId);
    }

    @ResponseBody
    @GetMapping("/deleteitem/{id}")
    public int deleteItem(@PathVariable(value="id") int itemId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        db.deleteItem(itemId);
        return itemId;
    }

    @ResponseBody
    @GetMapping("/deletelist/{id}")
    public int listItem(@PathVariable(value="id") int listId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        db.deleteList(listId);
        return listId;
    }

    public boolean sendEmail(String sendTo, String usernameFrom, String listTitle, int userId, int listId) throws Exception {
        if (db.getEmail(userId).equals(sendTo)) {
            return false;
        }

        int sendToId = db.getUserId(sendTo);
        boolean defaultUser = db.isDefaultUser(sendToId);

        try {
            System.out.println("attempt send");
            System.out.println(sendTo);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper email = new MimeMessageHelper(mimeMessage);
            email.setFrom("giftandpresentideas@gmail.com");
            email.setTo(sendTo);
            email.setSubject(usernameFrom + " has invited you to look at their list!");


            if (defaultUser) {
                email.setText(CreateEmail.createEmail(usernameFrom, listTitle), true);
            } else {
                email.setText(CreateEmail.createEmailGuest(usernameFrom, listTitle, sendTo), true);
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    @GetMapping("/resetpw")
    public String reset(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        return "resetpw";
    }

    @ResponseBody
    @PostMapping(value="/resetpw",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String resetPassword(@RequestBody String email, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String emailRemoveJson = email.substring(10, email.length()-2);
        db.resetPassword(emailRemoveJson);
        return gson.toJson("Successfully reset pw for " + emailRemoveJson);
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
            String username = login.getUsername();
            String enteredPassword = login.getPassword();
            String email = db.getEmailFromUsernameLogin(username);

            HashMap<String, String> userDetails = db.getuserDetailsFromEmail(email);
            String salt = userDetails.get("salt");
            String hashedPassword = userDetails.get("hashedPassword");

            if (hashedPassword.equals(PasswordSecurity.hashString(enteredPassword + salt))) {
                // User credentials OK.
                ListAppState state = getOrCreateSession(req, resp);
                state.setUserName(userDetails.get("userName"));
                state.setUserId(Integer.parseInt(userDetails.get("userId")));
                state.setEmail(email);

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
        private String email;

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

        @Nullable
        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
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
