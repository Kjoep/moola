package be.echostyle.moola.peristence.cache;

import be.echostyle.moola.peer.Peer;
import be.echostyle.moola.peer.PeerRepository;

import java.util.List;

public class CachedPeerRepository implements PeerRepository {

    private PeerRepository target;
    private Cache<String, Peer> cache = new MapCache<>();

    public CachedPeerRepository(PeerRepository target) {
        this.target = target;
    }

    @Override
    public Peer getPeer(String id) {
        return cache.get(id, ()->target.getPeer(id));
    }

    @Override
    public List<Peer> findPeers(String q) {
        List<Peer> r = target.findPeers(q);
        for (Peer peer:r){
            cache.put(peer.getId(), peer);
        }
        return r;
    }

}
