package com.github.ajoecker.gauge.services;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.orDefault;

/**
 * Interface to define multiple configurations.
 */
public interface ConfigurationSource {
    /**
     * Returns the string that separates multiple values e.g. when verifying a response.
     * <p>
     * The default value is <code>,</code> and is read in a Gauge project via the environment variable
     * <code>gauge.service.separator</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     * Then "popular_artists.artists.name" must contain "Pablo Picasso, Banksy"
     * </pre>
     *
     * @return the separator between multiple values
     */
    default String separator() {
        return orDefault("gauge.service.separator", ",");
    }

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
