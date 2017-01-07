package be.echostyle.moola;

public interface AccountFactory {
    Account create(String id, String name, AccountType type);

    GroupedAccount createGrouped(String id, String name);
}
