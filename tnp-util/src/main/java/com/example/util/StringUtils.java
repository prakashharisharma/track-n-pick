package com.example.util;

public class StringUtils {

    public static String format(String template, Object... values) {

        if (template == null || values == null) return template;

        StringBuilder result = new StringBuilder();
        int valueIndex = 0;
        int i = 0;

        while (i < template.length()) {
            if (i + 1 < template.length()
                    && template.charAt(i) == '{'
                    && template.charAt(i + 1) == '}') {
                if (valueIndex < values.length) {
                    result.append(values[valueIndex++]);
                } else {
                    result.append("{}");
                }
                i += 2;
            } else {
                result.append(template.charAt(i++));
            }
        }

        return result.toString();
    }
}
