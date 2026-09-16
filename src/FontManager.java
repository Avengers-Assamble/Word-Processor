import java.awt.Font;

public class FontManager {

    public static Font getFont(textrun run) {

        int style = Font.PLAIN;

        if (run.isBold()) {
            style |= Font.BOLD;
        }

        if (run.isItalic()) {
            style |= Font.ITALIC;
        }

        return new Font(
                run.getFontName(),
                style,
                run.getFontsize()
        );
    }
}

