package com.github.ajoecker.gauge.services;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.orDefault;

/**
 * Interface to define multiple configurations.
 */
public interface ConfigurationSource {
    /**
     * Returns the string that masks a variable in the query file.
     * <p>
     * Default string is <code>%</code> and is read in a Gauge project via the environment variable
     * <code>gauge.service.variable.mask</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     {
     *     popular_artists(size: %size%) {
     *         artists {
     *             name
     *             nationality
     *         }
     *     }
     * }
     * </pre>
     *
     * @return the mask or <code>%</code> if non is defined
     */
    default String variableMask() {
        return orDefault("gauge.service.variable.mask", "%");
    }

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
     * Returns the string that separates variables in the spec file to be replaced for an actual query.
     * <p>
     * The default value is <code>=</code> and is read in a Gauge project via the environment variable
     * <code>gauge.service.variable.separator</code>. If this variable is not existing or empty, the default value is taken.
     * <p>
     * For example
     * <pre>
     *     * When sending &lt;file:/src/test/resources/query&gt; with "size=4"
     * </pre>
     *
     * @return the separator between variables
     */
    default String variableSeparator() {
        return orDefault("gauge.service.variable.separator", "=");
    }


    /**
     * Masks a string with {@link #variableMask()} as prefix and suffix of this string
     *
     * @param s the string to mask
     * @return the masked string
     */
    default String mask(String s) {
        return variableMask() + s + variableMask();
    }

    /**
     * Unmasks a given {@link String} if it starts with {@link #variableMask()} or returns the String if not
     *
     * @param s string to unmask
     * @return unmasekd string
     */
    default String unmask(String s) {
        return isMasked(s) ? s.substring(variableMask().length(), s.length() - variableMask().length()) : s;
    }

    /**
     * Returns whether the given {@link String} is a variable and therefore masked.
     *
     * @param replacement the string to check
     * @return whether the string is a variable
     */
    default boolean isMasked(String replacement) {
        return replacement.startsWith(variableMask()) && replacement.endsWith(variableMask());
    }
}
