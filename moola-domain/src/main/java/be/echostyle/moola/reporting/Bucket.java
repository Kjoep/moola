package be.echostyle.moola.reporting;

import be.echostyle.moola.AccountEntryType;

public class Bucket {

    private String timeSlice;
    private AccountEntryType type;

    private long count;
    private long total;


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
}
