package be.echostyle.moola;

import be.echostyle.moola.filters.TransactionFilter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface GroupedAccount extends Account {

    default AccountType getType(){
        return AccountType.GROUPED;
    }

    Set<Account> getMembers();

    void addMember(Account member);

    void removeMember(Account member);

    default void setMembers(Set<Account> members){
        Set<Account> original = getMembers();

        for (Account originalMember : original)
            if (!members.contains(originalMember))
                removeMember(originalMember);

        for (Account newMember : members){
            if (!original.contains(newMember))
                addMember(newMember);
        }
    }

    @Override
    default List<AccountEntry> getTransactions(LocalDateTime from, LocalDateTime to) {
        return getMembers().stream().flatMap(member -> member.getTransactions(from, to).stream())
                .sorted(Comparator.comparing(AccountEntry::getTimestamp).thenComparing(AccountEntry::getId))
                .collect(Collectors.toList());
    }

    @Override
    default List<AccountEntry> getTransactions(String batchId) {
        return getMembers().stream().flatMap(member -> member.getTransactions(batchId).stream())
                .sorted(Comparator.comparing(AccountEntry::getTimestamp).thenComparing(AccountEntry::getId))
                .collect(Collectors.toList());
    }

    @Override
    default List<AccountEntry> getTransactions(LocalDateTime to, int count, int from) {
        //TODO: this count+from trick will work, but it's far from optimal
        return getMembers().stream().flatMap(member -> member.getTransactions(to, count + from, 0).stream())
                .sorted(Comparator.comparing(AccountEntry::getTimestamp).thenComparing(AccountEntry::getId))
                .skip(from)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    default List<AccountEntry> getTransactions(LocalDateTime to, TransactionFilter filter, int count, int from) {
        return getMembers().stream().flatMap(member -> member.getTransactions(to, filter, count + from, 0).stream())
                .sorted(Comparator.comparing(AccountEntry::getTimestamp).thenComparing(AccountEntry::getId))
                .skip(from)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    default AccountEntry getTransaction(String id) {
        for (Account member:getMembers()){
            AccountEntry r = member.getTransaction(id);
            if (r!=null) return r;
        }
        return null;
    }

    @Override
    default boolean contains(AccountEntry entry) {
        for (Account member : getMembers()){
            if (member.contains(entry))
                return true;
        }
        return false;
    }

    @Override
    default Set<String> getSimpleIds() {
        return getMembers().stream().flatMap(acc -> acc.getSimpleIds().stream()).collect(Collectors.toSet());
    }
}
