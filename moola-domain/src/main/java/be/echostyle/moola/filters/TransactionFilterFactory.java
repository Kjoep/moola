package be.echostyle.moola.filters;

public interface TransactionFilterFactory {

    TransactionFilter createFilter(String expression);
}
