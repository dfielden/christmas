package com.fielden.christmas_list.auth;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import com.google.common.hash.Hashing;

public class PasswordSecurity {
    public static String hashString(String s) {
        return Hashing.sha256().hashString(s, StandardCharsets.UTF_8).toString();
    }

    public static String[] createHashedPassword(String s) {
        final Random rand = new Random();
        String salt = String.format("%x", rand.nextLong());
        return new String[] {hashString(s + salt), salt};
    }

    public static boolean checkPassword(String enteredPassword, String salt, String hashedPassword) {
        return hashString(enteredPassword + salt).equals(hashedPassword);
    }

}