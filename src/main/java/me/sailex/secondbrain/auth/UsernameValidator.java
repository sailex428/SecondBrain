package me.sailex.secondbrain.auth;

import java.util.regex.Pattern;

public class UsernameValidator {

    private static final Pattern INVALID_USERNAME_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static boolean isValid(String username) {
        return VALID_USERNAME_PATTERN.matcher(username).matches();
    }

    public static String normalizeUsername(String username) {
        return INVALID_USERNAME_CHARS_PATTERN.matcher(username).replaceAll("_");
    }

}
