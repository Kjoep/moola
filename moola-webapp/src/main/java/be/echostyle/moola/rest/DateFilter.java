package be.echostyle.moola.rest;

import javax.ws.rs.BadRequestException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DateFilter {

    public abstract LocalDateTime from();
    public abstract LocalDateTime to();

    public static DateFilter parse(String string, Clock clock) {
        for (DateFilterParser parser : PARSERS){
            if (parser.accept(string))
                return parser.parse(string, clock);
        }
        throw new BadRequestException("Bad date format: "+string);
    }

    private interface DateFilterParser {
        boolean accept(String string);
        DateFilter parse(String string, Clock clock);
    }

    private static final DateFilterParser[] PARSERS = {
            namedParser("7days", n -> n.truncatedTo(ChronoUnit.DAYS).minusDays(7), n -> n.truncatedTo(ChronoUnit.DAYS).plusDays(1)),
            namedParser("30days", n -> n.truncatedTo(ChronoUnit.DAYS).minusDays(30), n -> n.truncatedTo(ChronoUnit.DAYS).plusDays(1)),
            namedParser("1month", n -> n.truncatedTo(ChronoUnit.DAYS).minusMonths(1), n -> n.truncatedTo(ChronoUnit.DAYS).plusMonths(1)),
            namedParser("12months", n -> n.truncatedTo(ChronoUnit.DAYS).minusMonths(12), n -> n.truncatedTo(ChronoUnit.DAYS).plusMonths(1)),
            new ComplexParser()
    };

    private static DateFilterParser namedParser(String constant, Function<LocalDateTime, LocalDateTime> calcFrom, Function<LocalDateTime, LocalDateTime> calcTo){
        return new DateFilterParser() {
            public boolean accept(String string) {
                return constant.equals(string);
            }

            @Override
            public DateFilter parse(String string, Clock clock) {
                LocalDateTime now = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return new DateFilter() {
                    @Override
                    public LocalDateTime from() {
                        return calcFrom.apply(now);
                    }

                    @Override
                    public LocalDateTime to() {
                        return calcTo.apply(now);
                    }
                };
            }
        };
    }

    private static class ComplexParser implements DateFilterParser {

        Pattern pattern = Pattern.compile("^(\\d+-\\d+-\\d+)-(\\d+)(days|weeks|months|years)$");

        @Override
        public boolean accept(String string) {
            return pattern.matcher(string).find();
        }

        @Override
        public DateFilter parse(String string, Clock clock) {
            Matcher matcher = pattern.matcher(string);
            matcher.find();
            LocalDate toDate = LocalDate.parse(matcher.group(1));
            String type = matcher.group(3);
            int nr = Integer.parseInt(matcher.group(2));

            LocalDate end = clock.instant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1);
            LocalDate start = null;
            switch (type){
                case "days": start = end.minusDays(nr); break;
                case "weeks": start = end.minusWeeks(nr); break;
                case "months": start = end.minusMonths(nr); break;
                case "years": start = end.minusYears(nr); break;
            }
            LocalDateTime from = start.atStartOfDay();
            return new DateFilter() {
                @Override
                public LocalDateTime from() {
                    return from;
                }

                @Override
                public LocalDateTime to() {
                    return end.atStartOfDay();
                }
            };
        }
    }
}
