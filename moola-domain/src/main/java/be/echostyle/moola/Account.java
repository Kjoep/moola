package be.echostyle.moola;

import be.echostyle.moola.filters.TransactionFilter;
import be.echostyle.moola.reporting.Slice;
import be.echostyle.moola.reporting.SliceStrategy;
import be.echostyle.moola.reporting.TimeSlice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public interface Account {

    String getName();

    AccountType getType();

    String getId();

    List<AccountEntry> getTransactions(LocalDateTime from, LocalDateTime to);

    List<AccountEntry> getTransactions(LocalDateTime to, int count);

    List<AccountEntry> getTransactions(LocalDateTime to, TransactionFilter filter, int count);

    List<AccountEntry> getTransactions(String batchId);

    boolean contains(AccountEntry entry);

    void setName(String name);

    AccountEntry getTransaction(String transactonId);

    default List<TimeSlice> getSlices(LocalDateTime from, LocalDateTime to) {
        SliceStrategy sliceStrategy = SliceStrategy.findIdealStrategy(from, to);
        from = sliceStrategy.adaptFrom(from);
        to = sliceStrategy.adaptTo(to);

        Map<Slice, TimeSlice> buckets = new TreeMap<>();

        for (Slice slice : sliceStrategy.getSlices(from, to)){
            buckets.put(slice, new TimeSlice(slice));
        }

        List<AccountEntry> transactions = getTransactions(from, to);
        for (AccountEntry transaction : transactions) {
            Slice slice = sliceStrategy.getBucket(transaction.getTimestamp());
            TimeSlice bucket = buckets.get(slice);
            bucket.addTransaction(transaction);
        }

        return new ArrayList<>(buckets.values());
    }

    /**
     * @return all IDs of simple accounts contributing to this account
     */
    Set<String> getSimpleIds();
}
