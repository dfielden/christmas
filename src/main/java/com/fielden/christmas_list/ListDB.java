package com.fielden.christmas_list;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ListDB {

    private final Connection connect;

    public ListDB() throws Exception {
        connect = DriverManager.getConnection("jdbc:sqlite:christmaslist.db", "root", "");

        // Initiate db tables if they do not exist
        initiateTables();

    }

    public synchronized void signup(String email, String username, String hashedPassword, String salt) throws Exception {
        if (checkIfEmailExists(email)) {
            throw new IllegalStateException("Email is already in use. Please login or sign up with a different email address.");
        }

        String query = "INSERT INTO User (email, username, hashed_pw, salt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, salt);

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
        String query = "INSERT INTO List (user_id, title, time_created) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, listTitle);
            stmt.setLong(3, System.currentTimeMillis());

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

    public synchronized String getTitle(int listId, int userId) throws Exception {
        if (!checkUserOwnsList(listId, userId)) {
            throw new IllegalStateException("User does not have permission to access list");
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

        if (!checkUserOwnsList(listId, userId)) {
            throw new IllegalStateException("User does not have permission to access list");
        }

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

    private synchronized boolean checkUserOwnsList(int listId, int userId) throws Exception {
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


}