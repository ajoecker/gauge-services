package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.ConfigurationSource;
import com.github.ajoecker.gauge.services.Connector;
import com.google.common.base.Strings;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableCell;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.datastore.DataStoreFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
            String replacement = replace(keyValue[1], connector);
            query = doReplace(query, keyValue[0], replacement);
        }
        return query;
    }

    /**
     * Checks whether the given string is masked and therefore needs to be replaced. If not, the value is returned.
     * <p>
     * If it is masked, it first checks the {@link DataStoreFactory#getScenarioDataStore()} for an existing key and returns
     * the value of the key, if existing.
     * <p>
     * If not, it checks whether a previous response has been done and the key can be found there and returns the value,
     * if existing
     *
     * @param keyValue  the key
     * @param connector the {@link Connector}
     * @return an actual value
     */
    public static String replace(String keyValue, Connector connector) {
        if (configurationSource.isMasked(keyValue)) {
            String toLookFor = configurationSource.unmask(keyValue);
            Object saved = DataStoreFactory.getScenarioDataStore().get(toLookFor);
            if (saved != null) {
                return saved.toString();
            }
            if (connector.hasPreviousResponse()) {
                String prev = extractPathFromPreviousRequest(toLookFor, connector);
                if (!Strings.isNullOrEmpty(prev)) {
                    return prev;
                }
            }
        }
        return keyValue;
    }

    private static String extractPathFromPreviousRequest(String toLookFor, Connector connector) {
        Object path = connector.pathFromPreviousResponse(toLookFor);
        if (path instanceof List) {
            throw new IllegalArgumentException("variable path " + toLookFor + " is not a single value, but a list: " + path);
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
            query = doReplace(query, row.getCell("name"), replace(row.getCell("value"), connector));
        }
        return query;
    }

    /**
     * Replaces all masked values in the given query by the new value
     *
     * @param query    the query containing masked value
     * @param newValue the value to replace
     * @return the query with the new value
     */
    public static String replaceMasked(String query, String newValue) {
        return query.replaceAll("%.+%", newValue);
    }

    /**
     * Extracts the variable of query
     *
     * @param query the query
     * @return the variable
     */
    public static String extractPlaceholder(String query) {
        int indexOf = query.indexOf('%');
        if (indexOf > 0) {
            return query.substring(indexOf + 1, query.indexOf('%', indexOf + 1));
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
                .flatMap(s1 -> Arrays.stream(s1.split("=")))
                .collect(Collectors.toList());
    }
}
