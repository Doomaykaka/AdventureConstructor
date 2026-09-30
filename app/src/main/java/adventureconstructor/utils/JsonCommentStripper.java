package adventureconstructor.utils;

/** Removes JSONC-style comments while preserving comment markers inside JSON strings. */
public final class JsonCommentStripper {
    private JsonCommentStripper() {}

    public static String strip(String json) {
        StringBuilder result = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);

            if (inString) {
                result.append(current);
                if (escaped) escaped = false;
                else if (current == '\\') escaped = true;
                else if (current == '"') inString = false;
                continue;
            }

            if (current == '"') {
                inString = true;
                result.append(current);
            } else if (current == '/' && i + 1 < json.length() && json.charAt(i + 1) == '/') {
                i += 2;
                while (i < json.length() && json.charAt(i) != '\n' && json.charAt(i) != '\r') i++;
                if (i < json.length()) result.append(json.charAt(i));
            } else if (current == '/' && i + 1 < json.length() && json.charAt(i + 1) == '*') {
                i += 2;
                boolean closed = false;
                while (i < json.length()) {
                    char commentChar = json.charAt(i);
                    if (commentChar == '\n' || commentChar == '\r') result.append(commentChar);
                    if (commentChar == '*' && i + 1 < json.length() && json.charAt(i + 1) == '/') {
                        i++;
                        closed = true;
                        break;
                    }
                    i++;
                }
                if (!closed) throw new IllegalArgumentException("Unterminated block comment in JSON");
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }
}
