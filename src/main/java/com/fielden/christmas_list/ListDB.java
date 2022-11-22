package com.fielden.christmas_list;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ListDB {

    private final Connection connect;

    public ListDB(HashMap<String, String> users) throws Exception {
        connect = DriverManager.getConnection("jdbc:sqlite:christmaslist.db", "root", "");

        // Initiate db tables if they do not exist
        initiateTables();

        // Add users if they have not been added
        addUsers(users);
    }

    public synchronized void signup(String email, String username, String hashedPassword, String salt) throws Exception {
        if (!checkIfEmailExists(email)) {
            throw new IllegalStateException(username + " is not a valid username. Please use the name assigned to you via email");
        }

        String query = "Update User SET " +
                "hashed_pw = ?, " +
                "salt = ?, " +
                "setup = ?" +
                "WHERE email = ? AND username = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setBoolean(3, true);
            stmt.setString(4, email);
            stmt.setString(5, username);

            stmt.executeUpdate();
        }
    }

    private synchronized boolean checkIfEmailExists(String email) throws Exception {
        String query = "SELECT * FROM User WHERE email = ?";
        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        }
    }

    public synchronized int createList(int userId, String listTitle) throws Exception {
        String query = "INSERT INTO List (user_id, title, time_created, shared) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, listTitle);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setBoolean(4, false);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new IllegalStateException("Unable to get generated key for added list");
            }
            return rs.getInt(1);
        }
    }

    public synchronized int addItem(int listId, ItemInList item) throws Exception {
        String query = "INSERT INTO Item (list_id, product, price, location, url, additional_info, selected, selected_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, listId);
            stmt.setString(2, item.getProduct());
            stmt.setDouble(3, item.getPrice());
            stmt.setString(4, item.getLocation());
            stmt.setString(5, item.getUrl());
            stmt.setString(6, item.getAdditionalInfo());
            stmt.setBoolean(7, false);

            stmt.setInt(8, -1);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new IllegalStateException("Unable to get generated key for added Item");
            }
            return rs.getInt(1);
        }
    }

    public synchronized int addUser(String username, String email, boolean defaultUser) throws Exception {
        String query = "INSERT INTO User (username, email, setup, default_user) VALUES (?, ?, ?, ?)";

        // if user already added, return the id
        int userId = getUserId(email);
        if (userId > -1) {
            return userId;
        }

        // otherwise add user to db

        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setBoolean(3, false);
            stmt.setBoolean(4, defaultUser);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new IllegalStateException("Unable to get generated key for added User");
            }
            return rs.getInt(1);
        }
    }

    public synchronized int addCustomShare(int userId, int sharedUserId, String sharedEmail) throws Exception {
        String query = "INSERT INTO CustomUser (user_id, shared_with, email) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, sharedUserId);
            stmt.setString(3, sharedEmail);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new IllegalStateException("Unable to get generated key for added User");
            }
            return rs.getInt(1);
        }
    }

    public synchronized int getUserId(String email) throws Exception {
        String query = "SELECT * FROM User WHERE email = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public synchronized void setListAsShared(int listId) throws Exception {
        String query = "UPDATE List SET " +
                "shared = true " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.executeUpdate();
        }
    }

    public synchronized boolean checkListIsShared(int listId) throws Exception {
        String query = "SELECT * FROM List WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getBoolean("shared")) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized String getTitle(int listId, int userId) throws Exception {
        if (!(checkUserOwnsList(listId, userId) || checkUserCanViewList(listId, userId))) {
            throw new IllegalStateException("User does not have permission to view list");
        }

        String query = "SELECT * FROM List WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("title");
            }
        }
        throw new IllegalStateException("No title found for id " + listId);
    }

    public synchronized String getEmailFromUsernameLogin(String username) throws Exception {

        String query = "SELECT * FROM User WHERE username = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getBoolean("setup")) {
                    return rs.getString("email");
                } else {
                    throw new IllegalStateException("User: " + username + " has not signed up yet...");
                }
            }
        }
        throw new IllegalStateException(username + " is not a valid username. Please use the name assigned to you via email.");
    }

    public synchronized String getEmailFromUsernameSignup(String username) throws Exception {

        String query = "SELECT * FROM User WHERE username = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }
        }
        throw new IllegalStateException(username + " is not a valid username. Please use the name assigned to you via email.");
    }

    public synchronized HashMap<Integer, ItemInList> getListAndHideSelectedStatus(int listId, int userId) throws Exception {
        HashMap<Integer, ItemInList> items = getList(listId, userId);
        for (ItemInList item : items.values()) {
            item.setSelected(false);
            item.setSelectedBy(0);
        }
        return items;
    }

    public synchronized HashMap<Integer, ItemInList> getList(int listId, int userId) throws Exception {
        HashMap<Integer, ItemInList> items = new HashMap<>();
        String query = "SELECT * FROM Item WHERE list_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String product = rs.getString("product");
                double price = rs.getDouble("price");
                String location = rs.getString("location");
                String url = rs.getString("url");
                String additionalInfo = rs.getString("additional_info");
                boolean selected = rs.getBoolean("selected");
                int selectedBy = rs.getInt("selected_by");

                ItemInList item = new ItemInList(product, price, location, url, additionalInfo, selected, selectedBy);
                items.put(id, item);
            }
        }
        return items;
    }

    public synchronized ArrayList<String> getCustomContacts(int userId) throws Exception {
        ArrayList<String> customContacts = new ArrayList<>();
        String query = "SELECT * FROM CustomUser WHERE user_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customContacts.add(rs.getString("email"));
            }
        }
        return customContacts;
    }

    public synchronized boolean checkItemSelectedStatus(int itemId) throws Exception {
        String query = "SELECT * FROM Item WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("selected");
            }
        }
        return false;
    }

    public synchronized HashMap<Integer, ListHeader> getMyLists(int userId) throws Exception {
        HashMap<Integer, ListHeader> lists = new HashMap<>();

        String query = "SELECT * FROM List WHERE user_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                long timeCreated = rs.getLong("time_created");

                lists.put(id, new ListHeader(title, timeCreated));
            }
        }
        return lists;
    }

    public synchronized String getUsername(int userId) throws Exception {

        String query = "SELECT * FROM User WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("username");
            }
        }
        throw new IllegalStateException("No user name found for id: " + userId);
    }

    public synchronized String getEmail(int userId) throws Exception {

        String query = "SELECT * FROM User WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }
        }
        throw new IllegalStateException("No user name found");
    }

    public synchronized Boolean isDefaultUser(int userId) throws Exception {

        String query = "SELECT * FROM User WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("default_user");
            }
        }
        throw new IllegalStateException("No user name found");
    }

    public synchronized void updateEmailSent(String email, int listId) throws Exception {
        String query = "UPDATE SharedWith SET email_sent = ? WHERE email_address = ? AND list_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setBoolean(1, true);
            stmt.setString(2, email);
            stmt.setInt(3, listId);

            stmt.executeUpdate();
        }
    }

    public synchronized boolean checkEmailSent(String email, int listId) throws Exception {
        String query = "SELECT * FROM SharedWith WHERE email_address = ? AND list_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setInt(2, listId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getBoolean("email_sent")) {
                    return true;
                }
            }
            return false;
        }
    }

    public synchronized void updateItem(int itemId, ItemInList item) throws Exception {
        String query = "UPDATE Item SET " +
                "product = ?, " +
                "price = ?, " +
                "location = ?, " +
                "url = ?, " +
                "additional_info = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, item.getProduct());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getLocation());
            stmt.setString(4, item.getUrl());
            stmt.setString(5, item.getAdditionalInfo());
            stmt.setInt(6, itemId);

            stmt.executeUpdate();
        }
    }

    public synchronized boolean selectItem(int itemId, int userId) throws Exception {

        if (checkItemSelectedStatus(itemId)) {
            return true;
        }

        String query = "UPDATE Item SET " +
                "selected = ?, " +
                "selected_by = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setBoolean(1, true);
            stmt.setInt(2, userId);
            stmt.setInt(3, itemId);
            stmt.executeUpdate();
        }
        return false;
    }

    public synchronized void deselectItem(int itemId) throws Exception {
        String query = "UPDATE Item SET " +
                "selected = ?, " +
                "selected_by = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setBoolean(1, false);
            stmt.setInt(2, -1);
            stmt.setInt(3, itemId);
            stmt.executeUpdate();
        }
    }

    public synchronized void deleteItem(int itemId) throws Exception {
        String query = "DELETE from Item WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, itemId);
            stmt.executeUpdate();
        }
    }

    public synchronized void deleteList(int listId) throws Exception {
        String query = "DELETE from List WHERE id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.executeUpdate();
        }
        deleteAllSharedWithFromList(listId);
        deleteAllItemsFromList(listId);

    }

    public synchronized void deleteAllSharedWithFromList(int listId) throws Exception {
        String query = "DELETE from Item WHERE list_id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.executeUpdate();
        }
    }

    public synchronized void deleteAllItemsFromList(int listId) throws Exception {
        String query = "DELETE from SharedWith WHERE list_id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.executeUpdate();
        }
    }

    public synchronized int addSharedWith(int listId, String emailAddress) throws Exception {
        String query = "INSERT INTO SharedWith (list_id, email_address, email_sent) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, listId);
            stmt.setString(2, emailAddress);
            stmt.setBoolean(3, false);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new IllegalStateException("Unable to get generated key for added email address");
            }
            return rs.getInt(1);
        }
    }

    public synchronized HashMap<Integer, ListHeaderShared> getSharedLists(String emailAddress) throws Exception {
        ArrayList<Integer> listIds = new ArrayList<>();
        HashMap<Integer, ListHeaderShared> lists = new HashMap<>();

        String query = "SELECT * FROM SharedWith WHERE email_address = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, emailAddress);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                listIds.add(rs.getInt("list_id"));
            }
        }

        for (Integer id : listIds) {
            query = "SELECT * FROM List WHERE id = ?";

            try (PreparedStatement stmt = connect.prepareStatement(query)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int listId = rs.getInt("id");
                    String title = rs.getString("title");
                    long timeCreated = rs.getLong("time_created");
                    int creatorId = rs.getInt("user_id");

                    if (!getEmail(creatorId).equals(emailAddress)) {
                        lists.put(listId, new ListHeaderShared(title, timeCreated, creatorId));
                    }

                }
            }
        }
        return lists;
    }

    public synchronized HashMap<String, Boolean> getSharedContacts(int listId) throws Exception {
        HashMap<String, Boolean> contacts = new HashMap<>();

        String query = "SELECT * FROM SharedWith WHERE list_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String email = rs.getString("email_address");
                String contact = getuserDetailsFromEmail(email).get("userName");
                contacts.put(contact, rs.getBoolean("email_sent"));
            }
        }
        return contacts;
    }

    public synchronized void deleteSharedEmail(int listId, String emailAddress) throws Exception {
        String query = "DELETE from SharedWith WHERE list_id = ? AND email_address = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.setString(2, emailAddress);
            stmt.executeUpdate();
        }
    }



    public synchronized boolean checkUserOwnsList(int listId, int userId) throws Exception {
        String query = "SELECT * FROM List WHERE id = ? AND user_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return true;
            }
            return false;
        }
    }

    public synchronized boolean checkUserCanViewList(int listId, int userId) throws Exception {
        String query = "SELECT * FROM SharedWith WHERE list_id = ?";
        String userEmail = getEmail(userId);

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, listId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getString("email_address").equals(userEmail)) {
                    return true;
                }
            }
            return false;
        }
    }

    private synchronized void initiateTables() throws Exception {
        ArrayList<String> queries = InitiateDBQueries.createTableQueries();
        for (String q : queries) {
            connect.createStatement().execute(q);
        }
    }

    public synchronized HashMap<String, String> getuserDetailsFromEmail(String email) throws Exception {
        HashMap<String, String> userDetails = new HashMap<>();

        String query = "SELECT * FROM User WHERE email = ?";
        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst() ) {
                throw new IllegalStateException("User not found: " + email);
            }

            while (rs.next()) {
                userDetails.put("userId", rs.getString("id"));
                userDetails.put("userName", rs.getString("username"));
                userDetails.put("email", rs.getString("email"));
                userDetails.put("hashedPassword", rs.getString("hashed_pw"));
                userDetails.put("salt", rs.getString("salt"));

            }
        }
        return userDetails;
    }

    private synchronized void addUsers(HashMap<String, String> users) throws Exception {
        String query = "SELECT * FROM User";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                for (String username : users.keySet()) {
                    addUser(username, users.get(username), true);
                }
            }

        }
    }

    public synchronized void resetPassword(String email) throws Exception {
        String query = "UPDATE User SET " +
                "salt = '', " +
                "hashed_pw = '' " +
                "WHERE email = ?";

        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

}