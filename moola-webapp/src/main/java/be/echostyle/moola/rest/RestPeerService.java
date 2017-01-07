package be.echostyle.moola.rest;

import be.echostyle.moola.rest.model.Peer;

import javax.ws.rs.*;
import java.util.List;

@Path("peers")
public interface RestPeerService {

    @PUT
    @Path("{id}")
    void updatePeer(@PathParam("id") String id, Peer peer);

    @GET
    @Path("{id}")
    Peer getPeer(@PathParam("id") String id);

    @GET
    List<Peer> findPeers(@QueryParam("q") String q);

}
