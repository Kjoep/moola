package be.echostyle.moola.rest.model;

public class TerminalInfo {

    private String name;
    private String location;
    private String card;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public static be.echostyle.moola.rest.model.TerminalInfo fromModel(be.echostyle.moola.TerminalInfo terminalInfo) {
        if (terminalInfo==null) return null;
        be.echostyle.moola.rest.model.TerminalInfo r = new be.echostyle.moola.rest.model.TerminalInfo();
        r.setName(terminalInfo.getName());
        r.setCard(terminalInfo.getCard());
        r.setLocation(terminalInfo.getLocation());
        return r;
    }


}
