package be.echostyle.moola.peristence.dbFunctions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class TimeFunctionsTest {

    @Test
    public void getsDay(){
        Timestamp ts = new Timestamp(1483741312726L);
        String day = TimeFunctions.day(ts);
        assertEquals("2017-01-06", day);
    }

    @Test
    public void getsWeek(){
        Timestamp ts = new Timestamp(1483741312726L);
        String week = TimeFunctions.week(ts);
        assertEquals("2017/1", week);
    }

    @Test
    public void getsMonth(){
        Timestamp ts = new Timestamp(1483741312726L);
        String month = TimeFunctions.month(ts);
        assertEquals("2017-01", month);
    }


}
