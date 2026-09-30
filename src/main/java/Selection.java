// By Ankita
public class Selection {
    private final Cursor anchor;
    private final Cursor active;

    public Selection(Cursor position) { this(position, position); }

    public Selection(Cursor anchor, Cursor active) {
        this.anchor = anchor;
        this.active = active;
    }

    public Cursor getAnchor() { return anchor; }
    public Cursor getActive() { return active; }
    public boolean isEmpty() { return anchor.equals(active); }

    public Cursor getStart() {
        return anchor.compareTo(active) <= 0 ? anchor : active;
    }

    public Cursor getEnd() {
        return anchor.compareTo(active) <= 0 ? active : anchor;
    }

    @Override
    public String toString() {
        return "Selection[" + getStart() + " -> " + getEnd() + "]";
    }
}