package be.echostyle.moola.rest;

import be.echostyle.moola.rest.model.*;
import be.echostyle.moola.rest.model.util.SelectItem;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Produces("application/json")
public interface RestAccountService {

    @GET
    @Path("accountTypes")
    List<SelectItem> getAccountTypes();

    @GET
    @Path("accounts")
    List<Account> listAccounts();

    @GET
    @Path("accounts/{accountId}")
    Account getAccount(@PathParam("accountId") String accountId);

    @DELETE
    @Path("accounts/{accountId}")
    void deleteAccount(@PathParam("accountId") String accountId);

    @PUT
    @Path("accounts/{accountId}")
    void updateAccount(@PathParam("accountId") String accountId, Account spec);

    /**
     * Upload an dump of transactions
     * @param accountId Account to upload into
     * @param content content to upload (usually csv)
     * @param format format descriptor.  If omitted, the default for the account is assumed.
     * @param formatSettings the parameters for the format (e.g. incoding, separator used, etc).  These are specific to the format used.
     * @return a reference to the upload batch that can be used to find back the uploaded transations
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("accounts/{accountId}/transactions/upload")
    Response upload(
            @PathParam("accountId") String accountId,
            @Multipart("file") byte[] content,
            @QueryParam("format") String format,
            @Context UriInfo formatSettings);

    @GET
    @Path("accounts/{accountId}/transactions")
    List<Transaction> getTransactions(
            @PathParam("accountId") String accountId,
            @QueryParam("batch") String batchReference,
            @QueryParam("filter") String filterExpression,
            @QueryParam("limit") @DefaultValue("200") Integer limit,
            @QueryParam("from") @DefaultValue("0") Integer from
            );

    @POST
    @Path("accounts/{accountId}/transactions/{transactionId}")
    void updateTransaction(
            @PathParam("accountId") String accountId,
            @PathParam("transactionId") String transactonId,
            Transaction spec);

    @GET
    @Path("accounts/{accountId}/slices")
    SlicedReport getSlices(
            @PathParam("accountId") String accountId,
            @QueryParam("from") String from,
            @QueryParam("to") String to);

    @GET
    @Path("uploadFormats")
    String[] getUploadFormats();

    //try with 1571a764244
}
