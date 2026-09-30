// aarohi's code
public class textrun {
    private String text;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private int fontSize;


    public textrun(String text) {
        this.text = text;
        this.bold = false;
        this.italic = false;
        this.underline = false;
        this.fontSize = 12;

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
    public int getFontsize( ){
        return fontSize;
    }
    public void setFontsize(int fontSize){
        this.fontSize=fontSize;
    }

}



