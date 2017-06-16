package be.echostyle.moola.persistence.dbFunctions;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class TimeFunctions {

    private TimeFunctions(){}

    public static String day(Timestamp ts){
        LocalDateTime ldt = ts.toLocalDateTime().truncatedTo(ChronoUnit.DAYS);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(ldt);
    }

    public static String month(Timestamp ts){
        LocalDateTime ldt = ts.toLocalDateTime().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
        return DateTimeFormatter.ofPattern("yyyy-MM").format(ldt);
    }

    public static String week(Timestamp ts){
        LocalDateTime ldt = ts.toLocalDateTime();
        int woy = ldt.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = ldt.get(ChronoField.YEAR);
        return year+"/"+woy;
    }

    public static String year(Timestamp ts){
        LocalDateTime ldt = ts.toLocalDateTime();
        return ""+ldt.get(ChronoField.YEAR);
    }

}
