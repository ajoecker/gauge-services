package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.ConfigurationSource;
import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class
 */
public final class ServiceUtil {
    public static final String COMMA_SEPARATED = "\\s*,\\s*";
    private static final Pattern compile = Pattern.compile("(%.+%)");

    // test-friendly
    static ConfigurationSource configurationSource = new ConfigurationSource() {
    };

    private ServiceUtil() {
        // utility class --> static
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
            query = queryWithoutVariables(query, keyValue[0], replaceVariables(keyValue[1], connector));
        }
        return query;
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
     * Replaces all variables from the given string by
     *
     * <ol>
     *     <li>Checking the value from {@link com.github.ajoecker.gauge.random.data.VariableStorage}</li>
     *     <li>Checking the value from a previous response</li>
     * </ol>
     *
     * @param v         the string with variables
     * @param connector the {@link Connector} used in the request
     * @return a replaced string with no variables
     */
    public static String replaceVariables(String v, Connector connector) {
        Matcher matcher = compile.matcher(v);
        String result = v;
        while (matcher.find()) {
            String variableValue = getVariableValue(configurationSource.unmask(matcher.group(1)), connector);
            String substring = v.substring(matcher.start(1), matcher.end(1));
            result = result.replace(substring, variableValue);
        }
        return result;
    }

    private static String getVariableValue(String variable, Connector connector) {
        Object saved = connector.getFromVariableStorage(variable);
        if (saved != null) {
            return saved.toString();
        }
        if (connector.hasPreviousResponse()) {
            String prev = connector.pathFromPreviousResponse(variable).toString();
            if (!"".equals(prev)) {
                return prev;
            }
        }
        return variable;
    }

    private static String queryWithoutVariables(String query, String key, String value) {
        return query.replace(configurationSource.mask(key.trim()), value.trim());
    }

    public static String[] split(String stringValue) {
        return stringValue.trim().split(COMMA_SEPARATED);
    }
}
