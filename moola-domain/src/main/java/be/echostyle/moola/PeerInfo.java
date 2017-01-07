package be.echostyle.moola;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

public class PeerInfo {

    private final String accountNr;
    private final String name;

    public PeerInfo(String accountNr, String name) {
        this.accountNr = accountNr;
        this.name = name;
    }

    public String getAccountNr() {
        return accountNr;
    }

    public String getName() {
        return name;
    }

    public static PeerInfo of(String accountNr, String name) {
        if (isBlank(accountNr) && isBlank(name)) return null;
        else return new PeerInfo(accountNr, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerInfo peerInfo = (PeerInfo) o;

        if (accountNr != null ? !accountNr.equals(peerInfo.accountNr) : peerInfo.accountNr != null) return false;
        return name != null ? name.equals(peerInfo.name) : peerInfo.name == null;

    }

    @Override
    public int hashCode() {
        int result = accountNr != null ? accountNr.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name + " ("+accountNr+")";
    }

    public static PeerInfo blank() {
        return new PeerInfo(null, null);
    }
}
