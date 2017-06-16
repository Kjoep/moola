package be.echostyle.moola.reporting;

public interface AggregatedQuery extends Query<AggregatedQuery, Bucket> {

    AggregatedQuery byDay();
    AggregatedQuery byWeek();
    AggregatedQuery byMonth();
    AggregatedQuery byYear();
    AggregatedQuery byType();
    AggregatedQuery byCategory();
}
