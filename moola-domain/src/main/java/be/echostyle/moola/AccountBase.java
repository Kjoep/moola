package be.echostyle.moola;

public abstract class AccountBase implements Account {

    protected final String id;
    protected String name;
    protected AccountType accountType;

    public AccountBase(String id, String name, AccountType accountType) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setType(AccountType accountType) {
        if (accountType == AccountType.GROUPED) throw new IllegalArgumentException("Cannot set a simple account to grouped");
        this.accountType = accountType;
    }

    @Override
    public AccountType getType() {
        return accountType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountBase that = (AccountBase) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String toString(){
        return "Account:"+id;
    }
}
