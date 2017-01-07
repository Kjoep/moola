package be.echostyle.moola.util;

public class TimestampGenerator implements IdGenerator {
    @Override
    public String generateId() {
        return Long.toHexString(System.currentTimeMillis());
    }
}
