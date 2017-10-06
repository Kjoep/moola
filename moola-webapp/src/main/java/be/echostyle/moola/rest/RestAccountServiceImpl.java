package be.echostyle.moola.rest;

import be.echostyle.moola.*;
import be.echostyle.moola.TerminalInfo;
import be.echostyle.moola.filters.FilterExpressionException;
import be.echostyle.moola.filters.FilterService;
import be.echostyle.moola.filters.FilterServiceImpl;
import be.echostyle.moola.parser.ImportException;
import be.echostyle.moola.reporting.TimeSlice;
import be.echostyle.moola.rest.model.*;
import be.echostyle.moola.rest.model.Category;
import be.echostyle.moola.rest.model.util.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDateTime.parse;

public class RestAccountServiceImpl implements RestAccountService {

    private static final Logger log = LoggerFactory.getLogger(RestAccountServiceImpl.class);
    private static final int DEFAULT_LIMIT = 200;

    private AccountService accountService;
    private ImportService importService;
    private FilterService filterService;
    private String baseUrl;

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public List<be.echostyle.moola.rest.model.Account> listAccounts() {
        return accountService.getAccounts().stream().map(this::accountFromModel).collect(Collectors.toList());
    }

    @Override
    public be.echostyle.moola.rest.model.Account getAccount(String accountId) {
        be.echostyle.moola.Account r = accountService.getAccount(accountId);
        if (r==null) throw new NotFoundException("No such account: "+accountId);
        return accountFromModel(r);
    }

    @Override
    public void deleteAccount(String accountId) {
        accountService.deleteAccount(accountId);
    }

    @Override
    public void updateAccount(String accountId, be.echostyle.moola.rest.model.Account spec) {
        be.echostyle.moola.Account original = accountService.getAccount(accountId);
        if (original==null)
            accountService.createAccount(accountId, spec.getName(), toModel(spec.getType()), spec.getGroupMembers());
        else if (original instanceof SimpleAccount && spec.getType()==AccountType.GROUPED){
            deleteAccount(accountId);
            accountService.createAccount(accountId, spec.getName(), toModel(spec.getType()), spec.getGroupMembers());
        }
        else if (original instanceof GroupedAccount && spec.getType()!=AccountType.GROUPED){
            deleteAccount(accountId);
            accountService.createAccount(accountId, spec.getName(), toModel(spec.getType()), spec.getGroupMembers());
        }
        else if (original instanceof SimpleAccount) {
            apply((SimpleAccount) original, spec);
        }
        else if (original instanceof GroupedAccount) {
            apply((GroupedAccount) original, spec);
        }
    }

    @Override
    public List<SelectItem> getAccountTypes() {
        return SelectItem.fromEnum(be.echostyle.moola.AccountType.class);
    }

    private be.echostyle.moola.AccountType toModel(AccountType type) {
        switch (type){
            case SAVINGS: return be.echostyle.moola.AccountType.SAVINGS;
            case CHECKING: return be.echostyle.moola.AccountType.CHECKING;
            case INVESTMENT: return be.echostyle.moola.AccountType.INVESTMENT;
            case GROUPED: return be.echostyle.moola.AccountType.GROUPED;
            default: return be.echostyle.moola.AccountType.CHECKING;
        }
    }

    @Override
    public Response upload(String accountId, byte[] content, String format, UriInfo parameters) {
        String ref = upload(accountId, content, format, parameters.getQueryParameters());
        return Response
                .ok()
                .location(buildUrl("accounts/{0}/upload/{1}", accountId, ref))
                .type(MediaType.APPLICATION_JSON)
                .entity("\""+ref+"\"")
                .build();
    }

    @Override
    public List<Transaction> getTransactions(String accountId, String batchReference, String filterExpression, Integer limit) {
        try {
            be.echostyle.moola.Account account = accountService.getAccount(accountId);
            if (account == null)
                throw new NotFoundException(Response.status(404).entity("No such account: " + accountId).build());

            if (limit == null) limit = DEFAULT_LIMIT;

            if (batchReference != null)
                return account.getTransactions(batchReference).stream().map(Transaction::fromModel).collect(Collectors.toList());
            else if (filterExpression != null)
                return account.getTransactions(LocalDateTime.now(), filterService.filter(filterExpression), limit).stream().map(Transaction::fromModel).collect(Collectors.toList());
            else
                return account.getTransactions(LocalDateTime.now(), limit).stream()
                        .sorted(Comparator.comparing(AccountEntry::getTimestamp).thenComparing(AccountEntry::getId))
                        .map(Transaction::fromModel)
                        .collect(Collectors.toList());
        } catch (FilterExpressionException e){
            throw new BadRequestException(Response.status(400).type(MediaType.TEXT_PLAIN).entity("Invalid filter: "+filterExpression+": "+e.getMessage()).build());
        }
    }

    @Override
    public void updateTransaction(String accountId, String transactonId, Transaction spec) {
        be.echostyle.moola.Account account = accountService.getAccount(accountId);
        if (account==null) throw new NotFoundException("No such account: "+accountId);

        AccountEntry transaction = account.getTransaction(transactonId);
        if (transaction==null) throw new NotFoundException("No such transaction: "+transactonId);

        if (spec.getDescription()!=null) transaction.setDescription(spec.getDescription());
        if (spec.getPeer()!=null) transaction.setPeer(accountService.getPeer(spec.getPeer().getId()));
        if (spec.getCategory()!=null) transaction.setCategory(accountService.getCategory(spec.getCategory().getId()));
    }

    @Override
    public SlicedReport getSlices(String accountId, String from, String to) {
        be.echostyle.moola.Account account = accountService.getAccount(accountId);
        if (account==null) throw new NotFoundException("No such account: "+accountId);

        List<TimeSlice> slices = account.getSlices(parse(from), parse(to));
        if (slices.isEmpty()) throw new WebApplicationException(Response.noContent().build());

        SlicedReport r = new SlicedReport();
        r.setTitle("Overview");
        r.setTimeSliceName(slices.iterator().next().getSlice().getTypeName());
        r.setData(slices.stream().map(this::sliceFromModel).collect(Collectors.toList()));
        r.setCategories(slices.stream().flatMap(s -> s.getCategories().stream())
                .distinct()
                .sorted(Comparator.comparing(be.echostyle.moola.category.Category::getName))
                .map(Category::fromModel)
                .collect(Collectors.toList()));
        return r;
    }

    @Override
    public String[] getUploadFormats() {
        return importService.getFormats().toArray(new String[0]);
    }

    private String upload(String accountId, byte[] content, String format, Map<String, List<String>> formatParameters){
        try {
            be.echostyle.moola.Account account = accountService.getAccount(accountId);
            if (account == null) throw new NotFoundException("No such account: " + accountId);
            if (!(account instanceof SimpleAccount)) throw new BadRequestException("Only simple accounts can be uploaded to");
            return importService.importData((SimpleAccount) account, content, format, formatParameters, entry -> filterService.scheduleAllFilters(entry));
        } catch (UnknownFormatException e){
            throw new NotFoundException(e.getMessage());
        } catch(ImportException e){
            throw new BadRequestException(e.getMessage());
        }
    }

    private URI buildUrl(String format, Object... parameters) {
        try {
            parameters = Stream.of(parameters)
                    .map(Object::toString)
                    .map(s -> {
                        try { return URLEncoder.encode(s, "utf-8"); }
                        catch (UnsupportedEncodingException e) { return s; }
                    })
                    .collect(Collectors.toList())
                    .toArray();
            return new URI(baseUrl + MessageFormat.format(format, parameters));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not build url", e);
        }
    }

    private be.echostyle.moola.rest.model.Account accountFromModel(be.echostyle.moola.Account account) {
        be.echostyle.moola.rest.model.Account r = new be.echostyle.moola.rest.model.Account();
        r.setId(account.getId());
        r.setName(account.getName());
        r.setType(AccountType.fromModel(account.getType()));
        return r;
    }

    private Slice sliceFromModel(TimeSlice timeSlice) {
        Slice r = new Slice();
        r.setSlice(timeSlice.getSlice().getName());
        r.setBalance(timeSlice.getBalance());
        r.setCategories(timeSlice.amountByCategory().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getId(), Map.Entry::getValue)));
        return r;
    }

    private void apply(SimpleAccount account, be.echostyle.moola.rest.model.Account spec) {
        account.setName(spec.getName());
        account.setType(toModel(spec.getType()));
    }

    private void apply(GroupedAccount account, be.echostyle.moola.rest.model.Account spec) {
        if (spec.getGroupMembers()==null) throw new BadRequestException("Grouped accounts must have members");
        account.setName(spec.getName());
        account.setMembers(spec.getGroupMembers().stream()
                .map(accountService::getAccount)
                .collect(Collectors.toSet()));
    }

    public void setImportService(ImportService importService) {
        this.importService = importService;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setFilterService(FilterService filterService) {
        this.filterService = filterService;
    }
}
