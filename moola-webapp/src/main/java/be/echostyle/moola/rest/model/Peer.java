package be.echostyle.moola.rest.model;

public class Peer {

    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Peer fromModel(be.echostyle.moola.peer.Peer peer) {
        if (peer==null) return null;
        Peer r = new Peer();
        r.id = peer.getId();
        r.name = peer.getName();
        return r;
    }
}
