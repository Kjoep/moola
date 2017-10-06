package be.echostyle.moola;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public interface SimpleAccount extends Account {

    AccountEntry addEntry(String batchId, LocalDateTime timestamp, int orderNr, long amount, long balance, String comment, AccountEntryType type, PeerInfo peerInfo, TerminalInfo terminalInfo);

    void setType(AccountType accountType);

    @Override
    default Set<String> getSimpleIds() {
        return Collections.singleton(getId());
    }
}
