package be.echostyle.moola.persistence.db;

public class TestConfigurationException extends RuntimeException {
    public TestConfigurationException() {
    }

    public TestConfigurationException(String message) {
        super(message);
    }

    public TestConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestConfigurationException(Throwable cause) {
        super(cause);
    }

}
