package com.fielden.christmas_list;

import java.util.ArrayList;

public final class InitiateDBQueries {

    private static String createUserTable() {
        return "CREATE TABLE IF NOT EXISTS User (" +
                "id INTEGER PRIMARY KEY NOT NULL, " +
                "username TEXT, " +
                "email TEXT, " +
                "hashed_pw TEXT, " +
                "salt TEXT)";
    }

    private static String createListTable() {
        return "CREATE TABLE IF NOT EXISTS List (" +
                "id INTEGER PRIMARY KEY NOT NULL, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "time_created INTEGER, " +
                "FOREIGN KEY (user_id) REFERENCES User(id)" +
                ")";
    }

    private static String createItemTable() {
        return "CREATE TABLE IF NOT EXISTS Item (" +
                "id INTEGER PRIMARY KEY NOT NULL, " +
                "list_id INTEGER, " +
                "product TEXT, " +
                "price TEXT, " +
                "location INTEGER, " +
                "url TEXT, " +
                "additional_info TEXT, " +
                "selected BOOLEAN, " +
                "selected_by INTEGER, " +
                "FOREIGN KEY (list_id) REFERENCES List(id)" +
                "FOREIGN KEY (selected_by) REFERENCES User(id)" +

                ")";
    }

    public static ArrayList<String> createTableQueries() {
        ArrayList<String> queries = new ArrayList<>();
        queries.add(createUserTable());
        queries.add(createListTable());
        queries.add(createItemTable());

        return queries;
    }
}
