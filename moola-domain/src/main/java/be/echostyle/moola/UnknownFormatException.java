package be.echostyle.moola;

public class UnknownFormatException extends IllegalArgumentException {
    public UnknownFormatException() {
    }

    public UnknownFormatException(String s) {
        super(s);
    }

    public UnknownFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownFormatException(Throwable cause) {
        super(cause);
    }
}
