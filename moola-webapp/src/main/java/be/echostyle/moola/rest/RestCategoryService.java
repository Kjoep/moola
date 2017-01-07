package be.echostyle.moola.rest;

import be.echostyle.moola.rest.model.Category;
import be.echostyle.moola.rest.model.Peer;

import javax.ws.rs.*;
import java.util.List;

@Path("categories")
public interface RestCategoryService {

    @PUT
    @Path("{id}")
    void updateCategory(@PathParam("id") String id, Category peer);

    @GET
    @Path("{id}")
    Category getCategory(@PathParam("id") String id);

    @GET
    List<Category> findCategories(@QueryParam("q") String q);

}
