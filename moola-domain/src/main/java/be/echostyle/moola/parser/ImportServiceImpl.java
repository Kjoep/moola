package be.echostyle.moola.parser;

import be.echostyle.moola.*;
import be.echostyle.moola.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ImportServiceImpl implements ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportServiceImpl.class);
    private IdGenerator batchIdGenerator;

    private Map<String, HistoryParser> parsers = new HashMap<>();

    @Override
    public String importData(SimpleAccount account, byte[] content, String format, Map<String, List<String>> formatParameters, Consumer<AccountEntry> postProcessor) {
        try {
            HistoryParser parser = parsers.get(format);
            if (parser == null)
                throw new UnknownFormatException("Unknown format: " + format + ". Known formats are " + parsers.keySet());

            String batchId = batchIdGenerator.generateId();

            log.info("Starting import: {}", batchId);

            Collection<AccountEntry> entries = parser.parse(new ByteArrayInputStream(content),
                    (timestamp, orderNr, amount, balance, comment, type, peerInfo, terminalInfo) -> {
                        AccountEntry entry = account.addEntry(batchId, timestamp, orderNr, amount, balance, comment, type, peerInfo, terminalInfo);
                        if (postProcessor != null)
                            postProcessor.accept(entry);
                        return entry;
                    });

            log.info("Import {}: {} lines parsed", batchId, entries.size());

            return batchId;
        } catch (IOException e){
            throw new ImportException("Cannot import data", e);
        }
    }

    @Override
    public Set<String> getFormats() {
        return parsers.keySet();
    }

    public void setBatchIdGenerator(IdGenerator batchIdGenerator) {
        this.batchIdGenerator = batchIdGenerator;
    }

    public void setParsers(Map<String, HistoryParser> parsers) {
        this.parsers = parsers;
    }
}
