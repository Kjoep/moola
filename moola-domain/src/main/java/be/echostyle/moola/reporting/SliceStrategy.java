package be.echostyle.moola.reporting;

import be.echostyle.moola.reporting.slicing.WeekOfYear;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

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
            return new WeekOfYear(timestamp.get(ChronoField.ALIGNED_WEEK_OF_YEAR));
        }
    };

    public static SliceStrategy findIdealStrategy(LocalDateTime from, LocalDateTime to) {
        return SliceStrategy.BY_WEEK;
    }

    public abstract LocalDateTime adaptFrom(LocalDateTime from);
    public abstract LocalDateTime adaptTo(LocalDateTime to);
    public abstract Slice getBucket(LocalDateTime timestamp);
}
