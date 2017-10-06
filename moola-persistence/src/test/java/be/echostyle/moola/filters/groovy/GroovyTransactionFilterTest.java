package be.echostyle.moola.filters.groovy;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryType;
import be.echostyle.moola.PeerInfo;
import be.echostyle.moola.TerminalInfo;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.filters.TransactionFilter;
import be.echostyle.moola.peer.Peer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class GroovyTransactionFilterTest {

    private GroovyFilterFactory factory = new GroovyFilterFactory();

    @Test
    public void passAll(){
        TransactionFilter all = factory.createFilter("true");

        assertTrue(all.match(new SimpleAcountEntry()));
    }

    @Test
    public void checksOnComment(){
        TransactionFilter all = factory.createFilter("comment.contains('beer')");

        assertTrue(all.match(new SimpleAcountEntry().withComment("I love beer!")));
        assertFalse(all.match(new SimpleAcountEntry().withComment("I love wine!")));

    }

    private static class SimpleAcountEntry extends AccountEntry {
        private String comment;

        public SimpleAcountEntry() {
            super(UUID.randomUUID().toString());
        }

        @Override
        public long getAmount() {
            return 0;
        }

        @Override
        public Peer getPeer() {
            return null;
        }

        @Override
        public LocalDateTime getTimestamp() {
            return null;
        }

        @Override
        public int getOrderNr() {
            return 0;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public long getBalance() {
            return 0;
        }

        @Override
        public AccountEntryType getType() {
            return null;
        }

        @Override
        public PeerInfo getPeerInfo() {
            return null;
        }

        @Override
        public TerminalInfo getTerminalInfo() {
            return null;
        }

        @Override
        public Category getCategory() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void setDescription(String description) {

        }

        @Override
        public void setPeer(Peer peer) {

        }

        @Override
        public void setCategory(Category category) {

        }

        public AccountEntry withComment(String comment) {
            this.comment = comment;
            return this;
        }
    }
}