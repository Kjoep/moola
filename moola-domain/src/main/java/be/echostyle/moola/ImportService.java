package be.echostyle.moola;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ImportService {

    /**
     * Import a textualized set of account entries
     *
     * @param account Target account
     * @param content Content as a textualized byte array
     * @param format format specifier (usually name of the financial institution; see {@link #getFormats()}
     * @param formatParameters Parameters for the format.  These are specific to the format itself
     * @param postProcess An optional postprocessor to be executed on every created entry
     * @return a Batch ID for this import job, added automatically to any non-duplicate entry found
     * @throws UnknownFormatException
     *  the given format specifier is not known to the system
     */
    String importData(SimpleAccount account, byte[] content, String format, Map<String, List<String>> formatParameters, Consumer<AccountEntry> postProcess) throws UnknownFormatException;

    Set<String> getFormats();

}
