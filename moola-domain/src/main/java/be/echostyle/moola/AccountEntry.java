package be.echostyle.moola;

import be.echostyle.moola.category.Category;
import be.echostyle.moola.peer.Peer;

import java.time.LocalDateTime;

public abstract class AccountEntry {

    protected final String id;

    public AccountEntry(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract long getAmount();

    public abstract Peer getPeer();

    public abstract LocalDateTime getTimestamp();

    /**
     * A numeric value to order by when multiple transactions share a timestamp.
     */
    public abstract int getOrderNr();

    public abstract String getComment();

    public abstract long getBalance();

    public abstract AccountEntryType getType();

    public abstract PeerInfo getPeerInfo();

    public abstract TerminalInfo getTerminalInfo();

    public abstract Category getCategory();

    /**
     * @return the current description for the transaction.  If never set, the comment is returned
     */
    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract void setPeer(Peer peer);

    public abstract void setCategory(Category category);
}
