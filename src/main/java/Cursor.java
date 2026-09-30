public class Cursor implements Comparable<Cursor> {
    private final int paragraphIndex;
    private final int offset;

    public Cursor(int paragraphIndex, int offset) {
        this.paragraphIndex = paragraphIndex;
        this.offset = offset;
    }

    public int getParagraphIndex() { return paragraphIndex; }
    public int getOffset() { return offset; }

    @Override
    public int compareTo(Cursor other) {
        if (paragraphIndex != other.paragraphIndex) {
            return Integer.compare(paragraphIndex, other.paragraphIndex);
        }
        return Integer.compare(offset, other.offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cursor)) return false;
        return compareTo((Cursor) o) == 0;
    }

    @Override
    public int hashCode() { return 31 * paragraphIndex + offset; }

    @Override
    public String toString() { return "(" + paragraphIndex + ", " + offset + ")"; }
}