package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerFactory;
import be.echostyle.moola.peer.PeerRepository;

import java.util.List;

public class DbPeerRepository extends JdbcRepository implements PeerRepository, PeerFactory {

    public DbPeerRepository() {
    }

    public DbPeerRepository(JdbcRepository other) {
        super(other);
    }

    @Override
    public Peer createPeer(String id, String name) {
        insert(DbPeer.PEER_TABLE, DbPeer.COL_ID, DbPeer.COL_NAME).values(id, name);
        return new DbPeer(this, id, name);
    }

    @Override
    public Peer getPeer(String id) {
        if (id==null) return null;
        return from(DbPeer.PEER_TABLE)
                .where(DbPeer.COL_ID + "=?", id)
                .one(this::mapPeer, DbPeer.ALL_COLS)
                .orElse(null);
    }

    @Override
    public List<Peer> findPeers(String q) {
        return from(DbPeer.PEER_TABLE)
                .where(DbPeer.COL_NAME + " like ?", "%"+q+"%")
                .list(this::mapPeer, DbPeer.ALL_COLS);
        //TODO: order by popularity
    }

    private Peer mapPeer(RowAdapter row){
        return new DbPeer(this, row.string(DbPeer.COL_ID), row.string(DbPeer.COL_NAME));
    }

}
