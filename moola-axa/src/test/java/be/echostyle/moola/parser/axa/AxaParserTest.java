package be.echostyle.moola.parser.axa;

import be.echostyle.moola.*;
import be.echostyle.moola.category.Category;
import be.echostyle.moola.peer.Peer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class AxaParserTest {

    @Test
    public void parsesFile() throws IOException {
        InputStream st = getClass().getResourceAsStream("example.csv");
        AxaParser parser = new AxaParser();

        AccountEntryFactory target = new AccountEntryFactory() {
            @Override
            public AccountEntry create(LocalDateTime timestamp, int orderNr, long amount, long balance, String comment, AccountEntryType type, PeerInfo peerInfo, TerminalInfo terminalInfo) {
                return new DummyAccountEntry(timestamp, orderNr, amount, comment, balance, type, peerInfo, terminalInfo);
            }
        };

        List<AccountEntry> output = parser.parse(st, target);

        assertEquals(19, output.size());

        assertEquals("503630237302", output.get(0).getComment());
        assertEquals("/C/ DOSSIER    B 315900    011231 A                  PERIODE: 201409; KINDERBIJSLAG   WWW.KIDS.PARTENA.BE", output.get(5).getComment());
        assertEquals(new TerminalInfo("KINEPOLIS LEUVEN", "LEUVEN", "7506376832160113"), output.get(10).getTerminalInfo());
        assertEquals(new PeerInfo("NL26RABO0120615789", "AZERTY B.V."), output.get(11).getPeerInfo());
    }

    private class DummyAccountEntry extends AccountEntry {
        private final LocalDateTime timestamp;
        private final int orderNr;
        private final long amount;
        private final String comment;
        private final long balance;
        private AccountEntryType type;
        private PeerInfo peerInfo;
        private TerminalInfo terminalInfo;

        public DummyAccountEntry(LocalDateTime timestamp, int orderNr, long amount, String comment, long balance, AccountEntryType type, PeerInfo peerInfo, TerminalInfo terminalInfo) {
            super(UUID.randomUUID().toString());
            this.timestamp = timestamp;
            this.orderNr = orderNr;
            this.amount = amount;
            this.comment = comment;
            this.balance = balance;
            this.type = type;
            this.peerInfo = peerInfo;
            this.terminalInfo = terminalInfo;
        }

        @Override
        public String toString() {
            return timestamp+" : " + comment+" -> "+amount+" -> "+balance;
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
        public String getComment() {
            return comment;
        }

        public long getBalance() {
            return balance;
        }

        public AccountEntryType getType() {
            return type;
        }

        @Override
        public Peer getPeer() {
            return null;
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
            return null;
        }

        @Override
        public void setDescription(String description) {
        }

        @Override
        public Category getCategory() {
            return null;
        }

        @Override
        public void setPeer(Peer peer) {

        }

        @Override
        public void setCategory(Category category) {

        }
    }
}