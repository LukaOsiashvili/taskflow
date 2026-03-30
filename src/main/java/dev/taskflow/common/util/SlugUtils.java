package dev.taskflow.common.util;

import java.text.Normalizer;
import java.util.function.Predicate;

public final class SlugUtils {

    private SlugUtils(){}

    // Normalize text for url safety
    // 1. NFD normalize to decompose accented chars
    // 2. Strip combining diacritical marks / accent parts
    // 3. Lowercase
    // 4. Replace anything not alphanumeric with hyphen
    // 5. Collapse consecutive hyphens
    // 6. String leading/trailing hyphens
    public static String toSlug(String name){
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-2{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    public static String toUniqueSlug(String name, Predicate<String> existsBySlug){
        String base = toSlug(name);
        String candidate = base;
        int suffix = 2;

        while (existsBySlug.test(candidate)){
            candidate = base + "-" + suffix++;
        }

        return candidate;
    }

}
