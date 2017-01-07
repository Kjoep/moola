package be.echostyle.moola.parser;

import be.echostyle.moola.AccountEntry;
import be.echostyle.moola.AccountEntryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

public interface HistoryParser {

    Collection<AccountEntry> parse(InputStream resourceAsStream, AccountEntryFactory entryFactory) throws IOException;

}
