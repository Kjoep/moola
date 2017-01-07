package be.echostyle.moola.parser;

import be.echostyle.moola.*;
import be.echostyle.moola.util.IdGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ImportServiceImpl implements ImportService {

    private IdGenerator batchIdGenerator;

    private Map<String, HistoryParser> parsers = new HashMap<>();

    @Override
    public String importData(SimpleAccount account, byte[] content, String format, Map<String, List<String>> formatParameters, Consumer<AccountEntry> postProcessor) {
        try {
            HistoryParser parser = parsers.get(format);
            if (parser == null)
                throw new UnknownFormatException("Unknown format: " + format + ". Known formats are " + parsers.keySet());

            String batchId = batchIdGenerator.generateId();

            Collection<AccountEntry> parse = parser.parse(new ByteArrayInputStream(content),
                    (timestamp, amount, balance, comment, type, peerInfo, terminalInfo) -> {
                        AccountEntry entry = account.addEntry(batchId, timestamp, amount, balance, comment, type, peerInfo, terminalInfo);
                        if (postProcessor != null)
                            postProcessor.accept(entry);
                        return entry;
                    });

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
