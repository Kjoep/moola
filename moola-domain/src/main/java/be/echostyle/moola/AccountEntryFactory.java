package be.echostyle.moola;

import java.time.LocalDateTime;

public interface AccountEntryFactory {
    AccountEntry create(LocalDateTime timestamp, long amount, long balance, String comment, AccountEntryType type, PeerInfo peerInfo, TerminalInfo terminalInfo);
}
