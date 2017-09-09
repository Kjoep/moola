package be.echostyle.moola.rest.model;

import java.math.BigDecimal;

public class Bucket {

    private String timeSlice;
    private String type;
    private Category category;
    private Peer peer;

    private long count;
    private BigDecimal total;

    public String getTimeSlice() {
        return timeSlice;
    }

    public String getType() {
        return type;
    }

    public Category getCategory() {
        return category;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public static Bucket fromModel(be.echostyle.moola.reporting.Bucket bucket){
        Bucket r = new Bucket();

        r.total = centsToFull(bucket.getTotal());
        r.peer = Peer.fromModel(bucket.getPeer());
        r.category = Category.fromModel(bucket.getCategory());
        r.timeSlice = bucket.getTimeSlice();
        r.type = bucket.getType()==null?null:bucket.getType().toString();
        r.count = bucket.getCount();

        return r;
    }

    private static BigDecimal centsToFull(long cents) {
        return new BigDecimal(cents).divide(new BigDecimal("100"));
    }

}
