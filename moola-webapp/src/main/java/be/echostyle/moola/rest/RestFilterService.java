package be.echostyle.moola.rest;


import be.echostyle.moola.rest.model.Filter;
import be.echostyle.moola.rest.model.FilterSpec;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("filters")
public interface RestFilterService {

    @GET
    List<Filter> list();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("new")
    void addFilter(FilterSpec filter);

    @POST
    @Path("{id}/apply")
    void applyFilter(@PathParam("id") String id, @QueryParam("apply") @DefaultValue("all") String applyMode);

    @GET
    @Path("{id}")
    Filter getFilter(@PathParam("id") String id);

    @DELETE
    @Path("{id}")
    void deleteFilter(@PathParam("id") String id);

    @GET
    @Path("/rulesBacklog")
    int getRulesToProcess();
}
