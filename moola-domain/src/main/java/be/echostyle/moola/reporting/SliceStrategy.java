package be.echostyle.moola.reporting;

import be.echostyle.moola.reporting.slicing.WeekOfYear;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class SliceStrategy {

    private static final SliceStrategy BY_WEEK = new SliceStrategy() {
        @Override
        public LocalDateTime adaptFrom(LocalDateTime from) {
            return from.with(ChronoField.DAY_OF_WEEK, 1);
        }

        @Override
        public LocalDateTime adaptTo(LocalDateTime to) {
            return to.with(ChronoField.DAY_OF_WEEK, 7);
        }

        @Override
        public Slice getBucket(LocalDateTime timestamp) {
            TemporalField week = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            return new WeekOfYear(timestamp.get(week));
        }

        @Override
        public List<Slice> getSlices(LocalDateTime from, LocalDateTime to) {
            TemporalField week = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            if (from.compareTo(to)>=0) return Collections.emptyList();
            ArrayList<Slice> r = new ArrayList<>();
            for (LocalDateTime ts = adaptFrom(from); ts.compareTo(adaptTo(to)) <=0; ts = ts.plusWeeks(1)){
                r.add(new WeekOfYear(ts.get(week)));
            }
            return r;
        }
    };

    public static SliceStrategy findIdealStrategy(LocalDateTime from, LocalDateTime to) {
        return SliceStrategy.BY_WEEK;
    }

    public abstract LocalDateTime adaptFrom(LocalDateTime from);
    public abstract LocalDateTime adaptTo(LocalDateTime to);
    public abstract Slice getBucket(LocalDateTime timestamp);
    public abstract List<Slice> getSlices(LocalDateTime from, LocalDateTime to);
}
