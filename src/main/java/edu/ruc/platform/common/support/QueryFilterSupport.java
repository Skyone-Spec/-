package edu.ruc.platform.common.support;

import edu.ruc.platform.common.exception.BusinessException;

import java.util.Locale;

public final class QueryFilterSupport {

    private QueryFilterSupport() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeUpper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    public static boolean containsIgnoreCase(String text, String keyword) {
        return text != null
                && keyword != null
                && text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    public static <E extends Enum<E>> String requireEnumValue(Class<E> enumType, String rawValue, String messagePrefix) {
        String normalized = normalizeUpper(rawValue);
        if (normalized == null) {
            return null;
        }
        try {
            Enum.valueOf(enumType, normalized);
            return normalized;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(messagePrefix + rawValue);
        }
    }
}
