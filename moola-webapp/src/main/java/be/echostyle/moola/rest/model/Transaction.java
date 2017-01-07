package be.echostyle.moola.rest.model;

import be.echostyle.moola.rest.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private String id;
    private BigDecimal amount;
    private BigDecimal balance;
    private Category category;
    private Peer peer;
    private LocalDateTime timestamp;
    private String comment;
    private String description;
    private String type;
    private PeerInfo peerInfo;
    private TerminalInfo terminalInfo;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PeerInfo getPeerInfo() {
        return peerInfo;
    }

    public void setPeerInfo(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }

    public TerminalInfo getTerminalInfo() {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo) {
        this.terminalInfo = terminalInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public static Transaction fromModel(be.echostyle.moola.AccountEntry transaction){
        Transaction r = new Transaction();

        r.setId(transaction.getId());
        r.setAmount(centsToFull(transaction.getAmount()));
        r.setPeer(Peer.fromModel(transaction.getPeer()));
        r.setCategory(Category.fromModel(transaction.getCategory()));
        r.setTimestamp(transaction.getTimestamp());
        r.setComment(transaction.getComment());
        r.setBalance(centsToFull(transaction.getBalance()));
        r.setType(transaction.getType()==null?null:transaction.getType().toString());
        r.setPeerInfo(PeerInfo.fromModel(transaction.getPeerInfo()));
        r.setTerminalInfo(TerminalInfo.fromModel(transaction.getTerminalInfo()));
        r.setDescription(transaction.getDescription());

        return r;
    }

    private static BigDecimal centsToFull(long cents) {
        return new BigDecimal(cents).divide(new BigDecimal("100"));
    }

}
