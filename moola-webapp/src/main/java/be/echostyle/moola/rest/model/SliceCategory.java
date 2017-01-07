package be.echostyle.moola.rest.model;

public class SliceCategory {

    private String name;
    private long amount;

    public SliceCategory() {
    }

    public SliceCategory(String category, long value) {
        this.name = category;
        this.amount = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
