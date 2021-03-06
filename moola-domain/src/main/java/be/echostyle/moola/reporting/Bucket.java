package be.echostyle.moola.reporting;

import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.peer.Peer;

import java.math.BigDecimal;

public class Bucket {

    private String timeSlice;
    private AccountEntryType type;
    private Category category;
    private Peer peer;

    private long count;
    private long total;
    private long balance;

    public String getTimeSlice() {
        return timeSlice;
    }

    public void setTimeSlice(String timeSlice) {
        this.timeSlice = timeSlice;
    }

    public AccountEntryType getType() {
        return type;
    }

    public void setType(AccountEntryType type) {
        this.type = type;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Category getCategory() {
        return category;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getBalance() {
        return balance;
    }
}
