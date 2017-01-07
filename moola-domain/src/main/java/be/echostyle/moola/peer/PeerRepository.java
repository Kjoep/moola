package be.echostyle.moola.peer;

import javax.sql.DataSource;
import java.util.List;

public interface PeerRepository {

    Peer getPeer(String id);

    List<Peer> findPeers(String q);
}
