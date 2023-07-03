package we.devs.forever.api.util.render.util;

import java.util.Random;
import java.util.regex.Pattern;

public
class TextUtil {


    public static final String SECTIONSIGN = "\u00A7";
    public static final String BLACK = SECTIONSIGN + "0";
    public static final String DARK_BLUE = SECTIONSIGN + "1";
    public static final String DARK_GREEN = SECTIONSIGN + "2";
    public static final String DARK_AQUA = SECTIONSIGN + "3";
    public static final String DARK_RED = SECTIONSIGN + "4";
    public static final String DARK_PURPLE = SECTIONSIGN + "5";
    public static final String GOLD = SECTIONSIGN + "6";
    public static final String CUSTOM       = SECTIONSIGN + "z";
    public static final String GRAY = SECTIONSIGN + "7";
    public static final String DARK_GRAY = SECTIONSIGN + "8";
    public static final String BLUE = SECTIONSIGN + "9";
    public static final String GREEN = SECTIONSIGN + "a";
    public static final String AQUA = SECTIONSIGN + "b";
    public static final String RED = SECTIONSIGN + "c";
    public static final String LIGHT_PURPLE = SECTIONSIGN + "d";
    public static final String YELLOW = SECTIONSIGN + "e";
    public static final String WHITE = SECTIONSIGN + "f";
    public static final String OBFUSCATED = SECTIONSIGN + "k";
    public static final String BOLD = SECTIONSIGN + "l";
    public static final String STRIKE = SECTIONSIGN + "m";
    public static final String UNDERLINE = SECTIONSIGN + "n";
    public static final String ITALIC = SECTIONSIGN + "o";
    public static final String RESET = SECTIONSIGN + "r";
    public static final String RAINBOW_PLUS = SECTIONSIGN + "+";
    /** $ + "+" */
    public static final String RAINBOW_MINUS = SECTIONSIGN + "-";
    public static final String blank = " \u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592\u2592";

    /* This is how it looks:
	▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
	███▒█▒█▒███▒███▒███▒███
	█▒█▒█▒█▒█▒█▒█▒█▒█▒█▒█▒▒
	███▒███▒█▒█▒███▒█▒█▒███
	█▒▒▒█▒█▒█▒█▒█▒█▒█▒█▒▒▒█
	█▒▒▒█▒█▒███▒███▒███▒███
	▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
	*/
    public static final String line1 = " \u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588";
    public static final String line2 = " \u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592";
    public static final String line3 = " \u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588";
    public static final String line4 = " \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2588\u2592\u2592\u2592\u2588";
    public static final String line5 = " \u2588\u2592\u2592\u2592\u2588\u2592\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588\u2592\u2588\u2588\u2588";
    public static final String pword = " " + blank + "\n" + line1 + "\n" + line2 + "\n" + line3 + "\n" + line4 + "\n" + line5 + "\n" + blank;
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + SECTIONSIGN + "[0-9A-FK-OR]");
    private static final Random rand = new Random();
    public static String shrug = "\u00AF\\_(\u30C4)_/\u00AF";
    public static String disability = "\u267f";

    //public static final String heartUnicode = "\u2764";

    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String coloredString(String string, Color color) {
        String coloredString = string;
        switch (color) {
            case AQUA:
                coloredString = AQUA + coloredString + RESET;
                break;
            case WHITE:
                coloredString = WHITE + coloredString + RESET;
                break;
            case BLACK:
                coloredString = BLACK + coloredString + RESET;
                break;
            case DARK_BLUE:
                coloredString = DARK_BLUE + coloredString + RESET;
                break;
            case DARK_GREEN:
                coloredString = DARK_GREEN + coloredString + RESET;
                break;
            case DARK_AQUA:
                coloredString = DARK_AQUA + coloredString + RESET;
                break;
            case DARK_RED:
                coloredString = DARK_RED + coloredString + RESET;
                break;
            case DARK_PURPLE:
                coloredString = DARK_PURPLE + coloredString + RESET;
                break;
            case GOLD:
                coloredString = GOLD + coloredString + RESET;
                break;
            case DARK_GRAY:
                coloredString = DARK_GRAY + coloredString + RESET;
                break;
            case GRAY:
                coloredString = GRAY + coloredString + RESET;
                break;
            case BLUE:
                coloredString = BLUE + coloredString + RESET;
                break;
            case RED:
                coloredString = RED + coloredString + RESET;
                break;
            case GREEN:
                coloredString = GREEN + coloredString + RESET;
                break;
            case LIGHT_PURPLE:
                coloredString = LIGHT_PURPLE + coloredString + RESET;
                break;
            case YELLOW:
                coloredString = YELLOW + coloredString + RESET;
                break;
            default:
        }
        return coloredString;
    }

    public static String cropMaxLengthMessage(String s, int i) {
        String output = "";
        if (s.length() >= 256 - i) {
            output = s.substring(0, 256 - i);
        }
        return output;
    }
/*
    public static String generateRandomHexSuffix(int n) {
        StringBuffer sb = new StringBuffer();
        sb.append(" [");
        sb.append(Integer.toHexString((rand.nextInt() + 11) * rand.nextInt()).substring(0, n));
        sb.append(']');
        return sb.toString();
    }*/

    public
    enum Color {
        NONE, WHITE, BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW
    }

    /*public static String appendChatSuffixH(String message, String suffix) {
        message = cropMaxLengthMessage(message, suffix.length());
        message += suffix;
        return cropMaxLengthMessage(message);
    }

    public static String generateRandomHexSuffixH(int n) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(Integer.toHexString((rand.nextInt() + 11) * rand.nextInt()).substring(0, n));
        sb.append(']');
        return sb.toString();
    }

    public static String cropMaxLengthMessage(String s) {
        return cropMaxLengthMessage(s, 0);
    }

    public static String transformPlainToFancy(final String input) {

        String output = input.toLowerCase();

        output = output.replace("a", "\u1d00");
        output = output.replace("b", "\u0299");
        output = output.replace("c", "\u1d04");
        output = output.replace("d", "\u1d05");
        output = output.replace("e", "\u1d07");
        output = output.replace("f", "\u0493");
        output = output.replace("g", "\u0262");
        output = output.replace("h", "\u029c");
        output = output.replace("i", "\u026a");
        output = output.replace("j", "\u1d0a");
        output = output.replace("k", "\u1d0b");
        output = output.replace("l", "\u029f");
        output = output.replace("m", "\u1d0d");
        output = output.replace("n", "\u0274");
        output = output.replace("o", "\u1d0f");
        output = output.replace("p", "\u1d18");
        output = output.replace("q", "\u01eb");
        output = output.replace("r", "\u0280");
        output = output.replace("s", "\u0455");
        output = output.replace("t", "\u1d1b");
        output = output.replace("u", "\u1d1c");
        output = output.replace("v", "\u1d20");
        output = output.replace("w", "\u1d21");
        output = output.replace("x", "\u0445");
        output = output.replace("y", "\u028f");
        output = output.replace("z", "\u1d22");

        return output;

    }*/

}
