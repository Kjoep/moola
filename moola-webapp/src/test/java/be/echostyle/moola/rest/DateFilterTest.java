package be.echostyle.moola.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DateFilterTest {

    private Clock clock;

    public DateFilterTest() {
        ZoneId utc = ZoneId.of("UTC");
        clock = Clock.fixed(ZonedDateTime.of(2010,01,01,0,0,0,0, utc).toInstant(), utc);
    }

    @Test
    public void parsesFullRange(){
        DateFilter filter = DateFilter.parse("2010-01-03-2011-02-28", clock);
        assertEquals(LocalDateTime.of(2010, 1, 3, 0, 0, 0), filter.from());
        assertEquals(LocalDateTime.of(2011, 2, 28, 0, 0, 0), filter.to());
    }

    @Test
    public void parsesStartRange(){
        DateFilter filter = DateFilter.parse("2010-01-03-", clock);
        assertEquals(LocalDateTime.of(2010, 1, 3, 0, 0, 0), filter.from());
        assertNull(filter.to());
    }

    @Test
    public void parsesEndRange(){
        DateFilter filter = DateFilter.parse("-2011-02-28", clock);
        assertNull(filter.from());
        assertEquals(LocalDateTime.of(2011, 2, 28, 0, 0, 0), filter.to());
    }

}