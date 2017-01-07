package be.echostyle.moola.peer;

public abstract class Peer {

    public abstract String getId();
    public abstract String getName();
    public abstract void setName(String name);

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        return getId().equals(peer.getId());

    }

    public int hashCode() {
        return getId().hashCode();
    }

    public String toString(){
        return getId();
    }
}
