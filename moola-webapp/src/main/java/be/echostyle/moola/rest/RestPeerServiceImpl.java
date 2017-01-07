package be.echostyle.moola.rest;

import be.echostyle.moola.AccountService;
import be.echostyle.moola.rest.model.Peer;

import java.util.List;
import java.util.stream.Collectors;

public class RestPeerServiceImpl implements RestPeerService {

    private AccountService accountService;

    @Override
    public void updatePeer(String id, Peer spec) {
        be.echostyle.moola.peer.Peer peer = accountService.getPeer(id);
        if (peer==null){
            accountService.createPeer(id, spec.getName());
        }
        else {
            peer.setName(spec.getName());
        }
    }

    @Override
    public Peer getPeer(String id) {
        return fromModel(accountService.getPeer(id));
    }

    @Override
    public List<Peer> findPeers(String q) {
        return accountService.findPeers(q).stream().map(this::fromModel).collect(Collectors.toList());
    }

    private Peer fromModel(be.echostyle.moola.peer.Peer peer) {
        Peer r = new Peer();
        r.setName(peer.getName());
        r.setId(peer.getId());
        return r;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }
}
