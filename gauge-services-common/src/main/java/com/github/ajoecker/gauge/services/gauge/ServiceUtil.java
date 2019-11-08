package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.ConfigurationSource;
import com.github.ajoecker.gauge.services.Connector;
import com.google.common.base.Strings;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableCell;
import com.thoughtworks.gauge.TableRow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Utility class
 */
public final class ServiceUtil {
    private static final String COMMA_SEPARATED;

    // test-friendly
    static ConfigurationSource configurationSource = new ConfigurationSource() {
    };

    static {
        COMMA_SEPARATED = "\\s*" + separator() + "\\s*";
    }

    private ServiceUtil() {
        // utility class --> static
    }

    public static String orDefault(String envKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        return Strings.isNullOrEmpty(envValue) ? defaultValue : envValue;
    }

    /**
     * Returns the separator defined in {@link #configurationSource}
     *
     * @return the separator
     * @see ConfigurationSource#separator()
     */
    public static String separator() {
        return configurationSource.separator();
    }

    /**
     * Replaces all variables in the given query based on the given variables
     *
     * @param query     the query containing variables
     * @param variables the values of the variables
     * @param connector the {@link Connector} used in the request
     * @return the actual query
     */
    public static String replaceVariablesInQuery(String query, String variables, Connector connector) {
        String[] split = split(variables);
        for (String s : split) {
            String[] keyValue = s.split(configurationSource.variableSeparator());
            String replacement = extractReplacement(keyValue[1], connector);
            query = doReplace(query, keyValue[0], replacement);
        }
        return query;
    }

    private static String extractReplacement(String replacement, Connector connector) {
        if (connector.hasPreviousResponse() && configurationSource.isMasked(replacement)) {
            return extractPathFromPreviousRequest(replacement, connector);
        }
        return replacement;
    }

    private static String extractPathFromPreviousRequest(String replacement, Connector connector) {
        String variablePath = configurationSource.unmask(replacement);
        Object path = connector.pathFromPreviousResponse(variablePath);
        if (path instanceof List) {
            throw new IllegalArgumentException("variable path " + variablePath + " is not a single value, but a list: " + path);
        }
        return path.toString();
    }

    private static String doReplace(String query, String key, String replacement) {
        return query.replace(configurationSource.mask(key.trim()), replacement.trim());
    }

    /**
     * Replaces all variables in the given query based on the given variables
     *
     * @param query     the query containing variables
     * @param variables the values of the variables as a gauge table
     * @param connector the {@link Connector} used in the request
     * @return the actual query
     */
    public static String replaceVariablesInQuery(String query, Table variables, Connector connector) {
        List<TableRow> tableRows = variables.getTableRows();
        for (TableRow row : tableRows) {
            query = doReplace(query, row.getCell("name"), extractReplacement(row.getCell("value"), connector));
        }
        return query;
    }

    public static String replaceMasked(String query, String newValue) {
        return query.replaceAll(configurationSource.variableMask() + ".+" + configurationSource.variableMask(), newValue);
    }

    public static String extractPlaceholder(String query) {
        int indexOf = query.indexOf(configurationSource.variableMask());
        if (indexOf > 0) {
            return query.substring(indexOf + 1, query.indexOf(configurationSource.variableMask(), indexOf + 1));
        }
        return "";
    }

    /**
     * Splits the given string based on {@link #COMMA_SEPARATED}
     *
     * @param stringValue value to be splitted
     * @return the split array
     */
    static String[] split(String stringValue) {
        return stringValue.trim().split(COMMA_SEPARATED);
    }

    /**
     * Parses a {@link List} of {@link Map}s out of a string in the format of
     * <pre>
     *     {name: Pablo Picasso, nationality: Spanish}, {name: Banksy, nationality: British}
     * </pre>
     *
     * @param value map like string
     * @return list of maps with a single key mapping
     */
    static List<Map<String, String>> parseMap(String value) {
        String[] values = value.trim().split("}" + COMMA_SEPARATED);
        return stream(values).map(ServiceUtil::toMap).collect(Collectors.toList());
    }

    private static Map<String, String> toMap(String full) {
        String prepared = full.replace("{", "").replace("}", "");
        return stream(prepared.split(COMMA_SEPARATED))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
    }

    /**
     * Returns whether the given string is a map representation as expected in {@link #parseMap(String)}
     *
     * @param value the value
     * @return whether the value is a map representation
     */
    static boolean isMap(String value) {
        return value.contains("{") && value.contains("}");
    }

    /**
     * Returns a {@link Map} out from the given Gauge {@link TableRow}
     *
     * @param tableRow the table row
     * @return a map with the key = the table header and value = table cell value
     */
    static Map<String, String> fromTable(TableRow tableRow) {
        return tableRow.getTableCells().stream().collect(Collectors.toMap(TableCell::getColumnName, TableCell::getValue));
    }

    public static List<String> splitIntoKeyValueList(String s) {
        return Arrays.stream(s.split(configurationSource.separator()))
                .flatMap(s1 -> Arrays.stream(s1.split(configurationSource.variableSeparator())))
                .collect(Collectors.toList());
    }
}
