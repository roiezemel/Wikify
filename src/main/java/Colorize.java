import java.util.HashMap;
import java.util.Map;

public class Colorize {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    private static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    private static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    private static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    private static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    private static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static final Map<String, String> colors = new HashMap<>() {{
        put("black", ANSI_BLACK);
        put("red", ANSI_RED);
        put("green", ANSI_GREEN);
        put("yellow", ANSI_YELLOW);
        put("blue",  ANSI_BLUE);
        put("purple", ANSI_PURPLE);
        put("cyan", ANSI_CYAN);
        put("white", ANSI_WHITE);
        put("reset", ANSI_RESET);
    }};

    public static final Map<String, String> backgroundColors = new HashMap<>() {{
        put("black", ANSI_BLACK_BACKGROUND);
        put("red", ANSI_RED_BACKGROUND);
        put("green", ANSI_GREEN_BACKGROUND);
        put("yellow", ANSI_YELLOW_BACKGROUND);
        put("blue", ANSI_BLUE_BACKGROUND);
        put("purple", ANSI_PURPLE_BACKGROUND);
        put("cyan", ANSI_CYAN_BACKGROUND);
        put("white", ANSI_WHITE_BACKGROUND);
        put("reset", ANSI_RESET);
    }};

    /**
     * Colorize text.
     * @param text text to colorize
     * @param color a color
     * @return colorized text
     */
    public static String colorized(String text, String color) {
        return colors.get(color) + text + ANSI_RESET;
    }

    /**
     * Colorize text.
     * @param text text to colorize
     * @param color a color
     * @param bold whether to make the text bold
     * @return colorized text
     */
    public static String colorized(String text, String color, boolean bold) {
        return (bold ? "\u001B[1m" : "") + colors.get(color) + text + ANSI_RESET;
    }

    /**
     * Colorize text.
     * @param text text to colorize
     * @param color a color
     * @param backgroundColor a background color
     * @param bold whether to make the text bold
     * @return colorized text
     */
    public static String colorized(String text, String color, String backgroundColor, boolean bold) {
        return (bold ? "\u001B[1m" : "") + backgroundColors.get(backgroundColor) + colors.get(color) + text + ANSI_RESET;
    }

}
