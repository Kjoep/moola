package be.echostyle.moola.persistence.db;

import be.echostyle.moola.peer.Peer;

public class DbPeer extends Peer {

    static final String PEER_TABLE = "peer";
    static final String COL_ID = "id";
    static final String COL_NAME = "name";
    static final String[] ALL_COLS = {COL_ID, COL_NAME};

    private final String id;
    private String name;

    private final DbPeerRepository repository;

    public DbPeer(DbPeerRepository repository, String id, String name) {
        this.id = id;
        this.name = name;
        this.repository = repository;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        repository.update(PEER_TABLE, COL_ID, id).set(COL_NAME, name).perform();
        this.name = name;
    }
}
