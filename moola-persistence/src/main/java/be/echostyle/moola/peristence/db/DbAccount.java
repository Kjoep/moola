package be.echostyle.moola.peristence.db;

import be.echostyle.moola.Account;
import be.echostyle.moola.AccountBase;
import be.echostyle.moola.AccountType;

public abstract class DbAccount extends AccountBase implements Account {

    final static String COL_ID = "id";
    final static String COL_NAME = "name";
    final static String COL_TYPE = "type";
    final static String TABLE = "Account";

    protected final DbAccountRepository repository;
    protected final IdGenerator idGenerator;

    DbAccount(String id, String name, AccountType type, DbAccountRepository repository, IdGenerator idGenerator) {
        super(id, name, type);
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    void removeAll(){
        GroupedDbAccount.removeFromAllGroups(this);
    };

    @Override
    public void setName(String name) {
        super.setName(name);
        repository.update(TABLE, COL_ID, id).set(COL_NAME, name).perform();
    }

    @Override
    public void setType(AccountType accountType) {
        super.setType(accountType);
        repository.update(TABLE, COL_ID, id).set(COL_TYPE, accountType).perform();
    }
}
