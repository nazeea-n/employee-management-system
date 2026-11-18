package com.vei.ems.service;

/** Small helper that writes safe CSV cells. Plain Java with no Spring, so it is easy to unit test. */
public final class CsvUtil {

    private CsvUtil() { }

    /**
     * Makes one value safe for a CSV file:
     * - values with a comma, quote or new line are wrapped in quotes (quotes are doubled)
     * - values starting with = + - @ get a leading apostrophe, so Excel never runs them as a formula
     */
    public static String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /** Joins the cells of one row with commas. */
    public static String row(Object... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(cell(cells[i]));
        }
        return sb.toString();
    }
}
