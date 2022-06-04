package io.github.verils.gotemplate;

public class Recipient {
    private final String name;
    private final String gift;
    private final boolean attended;

    public Recipient(String name, String gift, boolean attended) {
        this.name = name;
        this.gift = gift;
        this.attended = attended;
    }

    public String getName() {
        return name;
    }

    public String getGift() {
        return gift;
    }

    public boolean isAttended() {
        return attended;
    }
}
