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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Utility class
 */
public final class ServiceUtil {
    private static final String COMMA_SEPARATED;
    private static final Pattern compile = Pattern.compile("(%.+%)");

    // test-friendly
    static ConfigurationSource configurationSource = new ConfigurationSource() {
    };

    static {
        COMMA_SEPARATED = "\\s*" + separator() + "\\s*";
    }

    private ServiceUtil() {
        // utility class --> static
    }

    /**
     * Returns the system environment variable of the given key or the default value if the key does not exists
     *
     * @param envKey       the environment key
     * @param defaultValue the fall back value
     * @return the environment value or default value
     */
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
            String[] keyValue = s.split("=");
            String replacement = replaceVariables(keyValue[1], connector);
            query = queryWithoutVariables(query, keyValue[0], replacement);
        }
        return query;
    }

    public static String replaceVariables(String v, Connector connector) {
        Matcher matcher = compile.matcher(v);
        if (matcher.find()) {
            String variableValue = getVariableValue(configurationSource.unmask(matcher.group(1)), connector);
            return new StringBuffer(v).replace(matcher.start(1), matcher.end(1), variableValue).toString();
        }
        return v;
    }

    private static String getVariableValue(String variable, Connector connector) {
        Object saved = connector.getFromVariableStorage(variable);
        if (saved != null) {
            return saved.toString();
        }
        if (connector.hasPreviousResponse()) {
            String prev = extractPathFromPreviousRequest(variable, connector);
            if (!Strings.isNullOrEmpty(prev)) {
                return prev;
            }
        }
        return variable;
    }

    private static String extractPathFromPreviousRequest(String toLookFor, Connector connector) {
        Object path = connector.pathFromPreviousResponse(toLookFor);
        if (path instanceof List) {
            throw new IllegalArgumentException("variable path " + toLookFor + " is not a single value, but a list: " + path);
        }
        return path.toString();
    }

    private static String queryWithoutVariables(String query, String key, String value) {
        return query.replace(configurationSource.mask(key.trim()), value.trim());
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
            query = queryWithoutVariables(query, row.getCell("name"), replaceVariables(row.getCell("value"), connector));
        }
        return query;
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
                .flatMap(s1 -> Arrays.stream(s1.split("=")))
                .collect(Collectors.toList());
    }
}
