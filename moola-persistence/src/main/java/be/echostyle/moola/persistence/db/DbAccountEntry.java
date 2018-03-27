package be.echostyle.moola.persistence.db;

import be.echostyle.dbQueries.JdbcRepository;
import be.echostyle.dbQueries.RowAdapter;
import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.PeerInfo;
import be.echostyle.moola.TerminalInfo;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.peer.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class DbAccountEntry extends AccountEntry {

    private static final Logger log = LoggerFactory.getLogger(DbAccountEntry.class);

    final static String TABLE = "AccTransaction";

    final static String COL_ID = "id";
    final static String COL_BATCH_ID = "batch_id";
    final static String COL_TIMESTAMP = "transaction_ts";
    final static String COL_ORDERNR = "order_nr";
    final static String COL_COMMENT = "comment";
    final static String COL_DESCRIPTION = "description";
    final static String COL_ACCOUNT_ID = "account_id";
    final static String COL_AMOUNT = "amount";
    final static String COL_BALANCE = "balance";
    final static String COL_TYPE = "type";
    final static String COL_PEER_ACCOUNTNR = "peer_accountNr";
    final static String COL_PEER_NAME = "peer_name";
    final static String COL_TERMINAL_NAME = "terminal_name";
    final static String COL_TERMINAL_LOCATION = "terminal_location";
    final static String COL_TERMINAL_CARD = "terminal_card";
    final static String COL_PEER_ID = "peer_id";
    final static String COL_CATEGORY_ID = "category_id";

    final static String[] ALL_COLS = {COL_ID, COL_BATCH_ID, COL_ACCOUNT_ID,
            COL_TIMESTAMP, COL_ORDERNR, COL_AMOUNT, COL_COMMENT, COL_DESCRIPTION, COL_BALANCE, COL_TYPE,
            COL_PEER_ACCOUNTNR, COL_PEER_NAME,
            COL_TERMINAL_NAME, COL_TERMINAL_LOCATION, COL_TERMINAL_CARD, COL_PEER_ID, COL_CATEGORY_ID};


    private final JdbcRepository jdbc;
    private final LocalDateTime timestamp;
    private final int orderNr;
    private final long amount;
    private final long balance;
    private final String comment;
    private String description;
    private final AccountEntryType type;

    private Peer peer;
    private Category category;
    private final PeerInfo peerInfo;
    private final TerminalInfo terminalInfo;

    DbAccountEntry(String id, JdbcRepository jdbc, LocalDateTime timestamp, int orderNr, long amount, long balance, String comment, PeerInfo peerInfo, TerminalInfo terminalInfo, AccountEntryType type, String description, Peer peer, Category category) {
        super(id);
        this.jdbc = jdbc;
        this.timestamp = timestamp;
        this.orderNr = orderNr;
        this.amount = amount;
        this.comment = comment;
        this.type = type;
        this.balance = balance;
        this.peerInfo = peerInfo;
        this.terminalInfo = terminalInfo;
        this.description = description;
        this.peer = peer;
        this.category = category;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public int getOrderNr() {
        return orderNr;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public AccountEntryType getType() {
        return type;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public String toString(){
        return id;
    }

    @Override
    public Peer getPeer() {
        return peer;
    }

    public Category getCategory() {
        return category == null ? Category.UNKNOWN : category;
    }

    @Override
    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    @Override
    public TerminalInfo getTerminalInfo() {
        return terminalInfo;
    }

    @Override
    public String getDescription() {
        return description == null ? comment : description;
    }

    @Override
    public void setDescription(String description) {
        jdbc.update(TABLE, COL_ID, this.id).set(COL_DESCRIPTION, description).perform();
        this.description = description;
    }

    @Override
    public void setPeer(Peer peer) {
        jdbc.update(TABLE, COL_ID, this.id).set(COL_PEER_ID, peer.getId()).perform();
        this.peer = peer;
    }

    @Override
    public void setCategory(Category category) {
        jdbc.update(TABLE, COL_ID, this.id).set(COL_CATEGORY_ID, category.getId()).perform();
        this.category = category;
    }
}
