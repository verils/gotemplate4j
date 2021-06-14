package com.github.verils.gotemplate.lex;

public class Item {

    private final ItemType type;
    private final String val;
    private final int pos;

    public Item(ItemType type, String val, int pos) {
        this.type = type;
        this.val = val;
        this.pos = pos;
    }

    public ItemType getType() {
        return type;
    }

    public String getValue() {
        return val;
    }
}
