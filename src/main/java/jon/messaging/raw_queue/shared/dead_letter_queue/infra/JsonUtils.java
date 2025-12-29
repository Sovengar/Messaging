package jon.messaging.raw_queue.shared.dead_letter_queue.infra;

import org.apache.commons.lang3.StringEscapeUtils;

public class JsonUtils {
    public static String adjustEscaping(final String data) {
        var dataWithoutEnclosingQuotes = removeEnclosingQuotes(data);
        return StringEscapeUtils.unescapeJava(dataWithoutEnclosingQuotes);
    }

    public static String removeEnclosingQuotes(String input) {
        if (input != null && input.length() >= 2 && input.startsWith("\"") && input.endsWith("\"")) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }

    public static String simulateDbEscape(String input) {
        String escaped = input.replace("\\", "\\\\");
        escaped = escaped.replace("'", "''");
        return escaped;
    }
}
