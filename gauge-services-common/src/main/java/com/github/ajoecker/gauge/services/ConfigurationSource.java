package com.github.ajoecker.gauge.services;

/**
 * Interface to define multiple configurations.
 */
public interface ConfigurationSource {
    /**
     * Masks a string with % as prefix and suffix of this string
     *
     * @param s the string to mask
     * @return the masked string
     */
    default String mask(String s) {
        return "%" + s + "%";
    }

    /**
     * Unmasks a given {@link String} if it starts with % or returns the String if not
     *
     * @param s string to unmask
     * @return unmasekd string
     */
    default String unmask(String s) {
        return isMasked(s) ? s.substring(1, s.length() - 1) : s;
    }

    /**
     * Returns whether the given {@link String} is a variable and therefore masked.
     *
     * @param replacement the string to check
     * @return whether the string is a variable
     */
    default boolean isMasked(String replacement) {
        return replacement.startsWith("%") && replacement.endsWith("%");
    }
}
