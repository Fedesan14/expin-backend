package com.fedesan14.expin_backend.common.utils;

import java.util.Locale;

public class EmailUtils {
    private EmailUtils() {}

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
