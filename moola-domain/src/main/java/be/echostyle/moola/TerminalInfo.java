package be.echostyle.moola;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

public class TerminalInfo {

    private final String name;
    private final String location;
    private final String card;

    public TerminalInfo(String name, String location, String card) {
        this.name = name;
        this.location = location;
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCard() {
        return card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerminalInfo that = (TerminalInfo) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        return card != null ? card.equals(that.card) : that.card == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (card != null ? card.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Card "+card+" in terminal "+name+" at "+location;
    }

    public static TerminalInfo of(String name, String location, String card) {
        if (isBlank(name) && isBlank(location) && isBlank(card)) return null;
        return new TerminalInfo(name, location, card);
    }

    public static TerminalInfo blank() {
        return new TerminalInfo(null, null, null);
    }
}
