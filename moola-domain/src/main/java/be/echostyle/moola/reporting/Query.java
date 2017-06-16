package be.echostyle.moola.reporting;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface Query<Q extends Query, T> {

    int count();
    List<T> range(int from, int limit);

    Q withPeer(Set<String> peerId);
    Q withCategory(Set<String> categoryId);
    Q withType(Set<AccountEntryType> type);
    Q withTimestamp(LocalDateTime from, LocalDateTime to);
    Q newestFirst();

    AggregatedQuery aggregate();
}
