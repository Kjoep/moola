package be.echostyle.moola.reporting;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EmptyQuery implements EntryQuery {
    @Override
    public EntryQuery withPeer(Set<String> peerId) {
        return this;
    }

    @Override
    public EntryQuery withCategory(Set<String> categoryId) {
        return this;
    }

    @Override
    public EntryQuery withType(Set<AccountEntryType> type) {
        return this;
    }

    @Override
    public EntryQuery withTimestamp(LocalDateTime from, LocalDateTime to) {
        return this;
    }

    @Override
    public EntryQuery newestFirst() {
        return this;
    }

    @Override
    public AggregatedQuery aggregate() {
        return new AggregatedQuery() {
            @Override
            public AggregatedQuery byDay() {
                return this;
            }

            @Override
            public AggregatedQuery byWeek() {
                return this;
            }

            @Override
            public AggregatedQuery byMonth() {
                return this;
            }

            @Override
            public AggregatedQuery byYear() {
                return this;
            }

            @Override
            public AggregatedQuery byType() {
                return this;
            }

            @Override
            public int count() {
                return 0;
            }

            @Override
            public List<Bucket> range(int from, int limit) {
                return Collections.emptyList();
            }

            @Override
            public AggregatedQuery withPeer(Set<String> peerId) {
                return this;
            }

            @Override
            public AggregatedQuery withCategory(Set<String> categoryId) {
                return this;
            }

            @Override
            public AggregatedQuery withType(Set<AccountEntryType> type) {
                return this;
            }

            @Override
            public AggregatedQuery withTimestamp(LocalDateTime from, LocalDateTime to) {
                return this;
            }

            @Override
            public AggregatedQuery newestFirst() {
                return this;
            }

            @Override
            public AggregatedQuery aggregate() {
                return this;
            }
        };
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public List<AccountEntry> range(int from, int limit) {
        return Collections.emptyList();
    }

}
