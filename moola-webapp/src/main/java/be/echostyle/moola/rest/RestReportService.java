package be.echostyle.moola.rest;

import javax.ws.rs.*;
import java.util.List;

@Produces("application/json")
public interface RestReportService {

    @GET
    @Path("accounts/{id}/reports/adhoc/count")
    int getCount(@PathParam("id") String accountId, @QueryParam("filter") List<String> filters, @QueryParam("grouping") List<String> grouping);

    @GET
    @Path("accounts/{id}/reports/adhoc/{pageNr}")
    List<?> getPage(@PathParam("id") String accountId, @PathParam("pageNr") int page, @QueryParam("filter") List<String> filters, @QueryParam("grouping") List<String> grouping);

}
