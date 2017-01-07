package be.echostyle.moola.rest.model;

public class PeerInfo {

    private String name;
    private String account;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public static be.echostyle.moola.rest.model.PeerInfo fromModel(be.echostyle.moola.PeerInfo peerInfo) {
        if (peerInfo==null) return null;
        be.echostyle.moola.rest.model.PeerInfo r = new be.echostyle.moola.rest.model.PeerInfo();
        r.setName(peerInfo.getName());
        r.setAccount(peerInfo.getAccountNr());
        return r;
    }


}
