package be.echostyle.moola;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface AccountRepository {

    SortedSet<Account> getAccounts();

    Account getAccount(String accountId);

    void removeAccount(Account account);

}
