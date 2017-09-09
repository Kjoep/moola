package be.echostyle.moola.rest;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.reporting.EntryQuery;
import be.echostyle.moola.reporting.Query;
import be.echostyle.moola.reporting.ReportService;
import be.echostyle.moola.reports.Paging;
import be.echostyle.moola.rest.model.Bucket;
import be.echostyle.moola.rest.model.Transaction;

import javax.ws.rs.BadRequestException;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

public class RestReportServiceImpl implements RestReportService {

    private static final int PAGE_LENGTH = 50;
    private ReportService reportService;

    @Override
    public int getCount(String accountId, List<String> filters, List<String> grouping) {
        return asPages(buildReport(accountId, filters, grouping)).totalPages(PAGE_LENGTH);
    }

    @Override
    public List<?> getPage(String accountId, int page, List<String> filters, List<String> grouping) {
        return mapRecords(asPages(buildReport(accountId, filters, grouping)).page(PAGE_LENGTH, page));
    }

    private Query buildReport(String accountId, List<String> filters, List<String> grouping) {
        Query report = reportService.report(accountId);
        for (String filter:filters){
            String[] parts = filter.split(":");
            String key = parts[0];
            Set<String> values = parts.length < 2 ? Collections.singleton(null) : new HashSet<>(Arrays.asList(parts[1].split(",")));
            switch (key) {
                case "peer":
                    report = report.withPeer(values);
                    break;
                case "category":
                    report = report.withCategory(values);
                    break;
                case "type":
                    report = report.withType(enumOrBadRequest(values, AccountEntryType.class));
                    break;
                case "date":
                    report = applyDateFilter(report, values.iterator().next());
                    break;
                default:
                    throw new BadRequestException("Unknown filter type: " + key);
            }
        }
        for (String group:grouping){
            switch (group) {
                case "date:day":
                    report = report.aggregate().byDay();
                    break;
                case "date:week":
                    report = report.aggregate().byWeek();
                    break;
                case "date:month":
                    report = report.aggregate().byMonth();
                    break;
                case "date:year":
                    report = report.aggregate().byYear();
                    break;
                case "category:category":
                    report = report.aggregate().byCategory();
                    break;
                case "type":
                    report = report.aggregate().byType();
                    break;
            }
        }
        if (grouping.isEmpty())
            report = report.newestFirst();
        return report;
    }

    private <T,Q extends Query<Q,T>> Q applyDateFilter(Q report, String value) {
        DateFilter df = DateFilter.parse(value, Clock.systemDefaultZone());
        return report.withTimestamp(df.from(), df.to());
    }

    private <T> Paging<T> asPages(final Query<?,T> eq) {
        int count = eq.count();
        return new Paging<T>() {
            @Override
            public int totalPages(int perPage) {
                return (int)Math.ceil(count /(double)perPage);
            }

            @Override
            public List<T> page(int perPage, int page) {
                return  eq.range(perPage*page, perPage);
            }
        };
    }


    private <T extends Enum<T>> Set<T> enumOrBadRequest(Set<String> values, Class<T> type) {
        try {
            return values.stream()
                    .filter(o -> o!=null)
                    .map(Optional::of)
                    .map(s -> Enum.valueOf(type, s.orElseThrow(IllegalArgumentException::new)))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e){
            throw new BadRequestException("Illegal value: "+values);
        }
    }

    private List<?> mapRecords(List<?> o) {
        return o.stream().map(this::mapRecord).collect(Collectors.toList());
    }

    private Object mapRecord(Object o) {
        if (o instanceof AccountEntry)
            return Transaction.fromModel((AccountEntry) o);
        else if (o instanceof be.echostyle.moola.reporting.Bucket)
            return Bucket.fromModel((be.echostyle.moola.reporting.Bucket) o);
        else return o;
    }

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }
}
