package be.echostyle.moola.filters;

public class FilterExpressionException extends IllegalArgumentException {
    private String filterExpression;

    public FilterExpressionException(String filterExpression) {
        super("Invalid expression: "+filterExpression);
        this.filterExpression = filterExpression;
    }
}
