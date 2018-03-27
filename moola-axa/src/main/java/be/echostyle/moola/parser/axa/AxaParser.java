package be.echostyle.moola.parser.axa;

import be.echostyle.moola.*;
import be.echostyle.moola.parser.HistoryParser;
import be.echostyle.moola.parser.ImportException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AxaParser implements HistoryParser {

    private static final Logger log = LoggerFactory.getLogger(AxaParser.class);
    private String encoding = "UTF8";

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    private enum Header {
        afschrift,
        datum_verrichting,
        datum_valuta,
        datum_boeking,
        bedrag,
        saldo_rekening,
        omschrijving_aard_verrichting,
        rekening_begunstigde,
        tegenpartij,
        naam_terminal,
        plaats_terminal,
        kaartnummer,
        mededeling,
        vervolg_mededeling,
        detail_verrichting
    }

    @Override
    public List<AccountEntry> parse(InputStream stream, AccountEntryFactory entryFactory) throws IOException {
        AtomicInteger index = new AtomicInteger(0);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding));
            readHeader(br);
            String[] headerNames = getHeaderNames();
            CSVParser csvRecords = new CSVParser(br, CSVFormat.DEFAULT.withDelimiter(';').withSkipHeaderRecord().withHeader(headerNames));
            return StreamSupport.stream(csvRecords.spliterator(), false)
                    .map(line -> this.parseLineSingle(new CsvLine(line, index.getAndIncrement()), entryFactory))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e){
            log.warn("Parse error for stream "+stream, e);
            throw new ImportException("Invalid format -- does not comply to AXA format");
        }
    }

    private String[] getHeaderNames() {
        return Stream.of(Header.values()).map(Enum::name).collect(Collectors.toList()).toArray(new String[0]);
    }

    private void readHeader(BufferedReader br) throws IOException {
        int emptyLines=0;
        while (emptyLines<2){
            String line = br.readLine();
            log.debug("Skipping header line: {}", line);
            if (StringUtils.isBlank(line)) emptyLines++;
        }
    }

    private AccountEntry parseLineSingle(CsvLine accountLine, AccountEntryFactory entryFactory) {
        return entryFactory.create(
                accountLine.timestamp(Header.datum_verrichting),
                accountLine.index(),
                accountLine.longCents(Header.bedrag),
                accountLine.longCents(Header.saldo_rekening),
                accountLine.strings("; ", Header.mededeling,  Header.vervolg_mededeling),
                mapToType(accountLine.string(Header.omschrijving_aard_verrichting)),
                PeerInfo.of(accountLine.string(Header.rekening_begunstigde), accountLine.string(Header.tegenpartij)),
                TerminalInfo.of(accountLine.string(Header.naam_terminal), accountLine.string(Header.plaats_terminal), cleanCard(accountLine.string(Header.kaartnummer)))
        );
    }

    /**
     * For some reason, axa sometimes exports the card number as ="123".  We cut off the junk here.
     */
    private String cleanCard(String card) {
        if (StringUtils.isBlank(card)) return card;
        Matcher matcher = Pattern.compile("^=\"(.*)\"$").matcher(card);
        if (matcher.find()){
            return matcher.group(1);
        }
        else return card;
    }

    private AccountEntryType mapToType(String typeString) {
        switch (typeString.toLowerCase()) {
            case "aankoop met axa bankkaart": return AccountEntryType.cardPayment;
            case "aankoop - bancontact": return AccountEntryType.cardPayment;
            case "aankoop - maestro": return AccountEntryType.cardPayment;
            case "aankp buitenl.mt axa bnkkrt": return AccountEntryType.cardPayment;
            case "geldopname met axa bankkaart": return AccountEntryType.withdrawal;
            case "geldopname - bancontact": return AccountEntryType.withdrawal;
            case "geldopname - maestro": return AccountEntryType.withdrawal;
            case "interne invordering": return AccountEntryType.managementCost;
            case "bijdrage zichtrekening": return AccountEntryType.managementCost;
            case "europese overschrijving": return AccountEntryType.transfer;
            case "europese domiciliëring": return AccountEntryType.fixedOrder;
            case "domiciliëring visa-kaart": return AccountEntryType.fixedOrder;
            case "Glob. verk. - Bancontact": return AccountEntryType.cardPayment;
            case "Europese overschr via Mobile": return AccountEntryType.transfer;
            default: {
                log.warn("Unknown entry type: {}", typeString);
                return AccountEntryType.unknown;
            }
        }
    }

    /**
     * Wrapper around csv record for easy type conversion
     */
    private static class CsvLine{
        CSVRecord wrapped;
        int index;

        public CsvLine(CSVRecord wrapped, int index) {
            this.wrapped = wrapped;
            this.index = index;
        }

        int index() {
            return index;
        }

        String string(Enum<?> col){
            return wrapped.get(col);
        }
        String string(Enum<?> col, String defaultValue){
            String r = string(col);
            if (StringUtils.isBlank(r)) return defaultValue;
            else return r;
        }
        String strings(String joiner, Enum<?>... cols){
            return Stream.of(cols)
                    .map(this::string)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(joiner));
        }

        long longCents(Enum<?> col){
            String v = string(col);
            if (StringUtils.isBlank(v)) return 0;
            v = v.replace(".","").replace(",",".");
            return new BigDecimal(v).multiply(new BigDecimal("100")).toBigInteger().longValue();
        }

        long longInt(Enum<?> col){
            return Long.parseLong(string(col));
        }

        public LocalDateTime timestamp(Enum<?> col) {
            String v = string(col);
            if (StringUtils.isBlank(v)) return null;
            return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(v)).atStartOfDay();
        }
    }

}
