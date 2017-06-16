package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupedDbAccount extends DbAccount implements GroupedAccount {

    private static final String MEMBER_TABLE = "AccGroupMembers";
    private static final String COL_GROUP = "GroupId";
    private static final String COL_MEMBER = "MemberId";

    public GroupedDbAccount(String id, String name, DbAccountRepository dbAccountRepository, IdGenerator idGenerator) {
        super(id, name, AccountType.GROUPED, dbAccountRepository, idGenerator);
    }

    @Override
    public Set<Account> getMembers() {
        Set<Account> r = repository.from(MEMBER_TABLE).where(COL_GROUP + " = ?", id).set(this::mapMember, COL_MEMBER);
        r.remove(null);
        return Collections.unmodifiableSet(r);
    }

    @Override
    public void addMember(Account member) {
        if (!(member instanceof DbAccount)) throw new IllegalArgumentException("Only DB accounts can be added to a grouped db account");
        repository.insert(MEMBER_TABLE, COL_GROUP, COL_MEMBER).values(id, member.getId());
    }

    @Override
    public void removeMember(Account member) {
        if (!(member instanceof DbAccount)) throw new IllegalArgumentException("Only DB accounts can be added to a grouped db account");
        repository
                .from(MEMBER_TABLE)
                .where(COL_GROUP+"=?", id)
                .where(COL_MEMBER+"=?", member.getId())
                .delete();
    }

    @Override
    void removeAll() {
        repository
                .from(MEMBER_TABLE)
                .where(COL_GROUP+"=?", id)
                .delete();
        super.removeAll();
    }

    private Set<String> allSimpleIds() {
        return allSimpleIds(this).collect(Collectors.toSet());
    }

    private Stream<String> allSimpleIds(GroupedAccount grouped){
        return getMembers().stream().flatMap(m -> {
            return
                m instanceof SimpleAccount ? Stream.of(m.getId()) :
                m instanceof GroupedDbAccount ? allSimpleIds((GroupedAccount) m) :
                Stream.empty();
        });
    }

    private Account mapMember(RowAdapter rowAdapter) {
        return repository.getAccount(rowAdapter.string(COL_MEMBER));
    }

    public static void removeFromAllGroups(DbAccount member) {
        member.repository
                .from(MEMBER_TABLE)
                .where(COL_MEMBER+"=?", member.getId())
                .delete();
    }
}
