package be.echostyle.moola.rest.model;

import java.util.List;
import java.util.Map;

public class Slice {

    private String slice;
    private Map<String, Long> categories;
    private long balance;

    public String getSlice() {
        return slice;
    }

    public void setSlice(String slice) {
        this.slice = slice;
    }

    public Map<String, Long> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Long> categories) {
        this.categories = categories;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
