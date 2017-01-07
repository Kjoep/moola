package be.echostyle.moola.reporting.slicing;

import be.echostyle.moola.reporting.Slice;

public class WeekOfYear implements Slice {
    private int weekOfYear;

    public WeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    @Override
    public String getName() {
        return ""+weekOfYear;
    }

    @Override
    public String getTypeName() {
        return "Week";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeekOfYear that = (WeekOfYear) o;

        return weekOfYear == that.weekOfYear;

    }

    @Override
    public int hashCode() {
        return weekOfYear;
    }

    @Override
    public int compareTo(Slice slice) {
        if (slice==null) return -1;
        if (slice instanceof WeekOfYear){
            return weekOfYear - ((WeekOfYear) slice).weekOfYear;
        }
        return -1;
    }
}
