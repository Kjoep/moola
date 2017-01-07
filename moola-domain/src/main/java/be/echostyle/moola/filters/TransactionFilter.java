package be.echostyle.moola.filters;

import be.echostyle.moola.AccountEntry;

public interface TransactionFilter {

    boolean match(AccountEntry entry);

}
