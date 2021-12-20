package io.github.verils.gotemplate.lex;

public class Item {

    private final ItemType type;
    private final String val;
    private final int pos;

    public Item(ItemType type, String val, int pos) {
        this.type = type;
        this.val = val;
        this.pos = pos;
    }

    public ItemType type() {
        return type;
    }

    public String value() {
        return val;
    }

    public int pos() {
        return pos;
    }

    @Override
    public String toString() {
        switch (type) {
            case EOF:
                return "EOF";
            case ERROR:
                return val;
            case KEYWORD:
                return '<' + val + '>';
            default:
                return val;
        }
    }
}
