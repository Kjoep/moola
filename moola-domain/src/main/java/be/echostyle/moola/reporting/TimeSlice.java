package be.echostyle.moola.reporting;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.category.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TimeSlice {

    private Slice slice;
    private List<AccountEntry> transactions = new ArrayList<>();

    public TimeSlice(Slice slice) {
        this.slice = slice;
    }

    public Slice getSlice() {
        return slice;
    }

    public void addTransaction(AccountEntry transaction) {
        this.transactions.add(transaction);
    }

    public long getBalance() {
        if (transactions.isEmpty()) return 0;
        return transactions.get(0).getBalance();
    }

    public Map<Category, Long> amountByCategory() {
        return transactions.stream().collect(Collectors.groupingBy(AccountEntry::getCategory, Collectors.summingLong(AccountEntry::getAmount)));
    }

    public Set<Category> getCategories() {
        return transactions.stream().map(AccountEntry::getCategory).collect(Collectors.toSet());
    }
}
