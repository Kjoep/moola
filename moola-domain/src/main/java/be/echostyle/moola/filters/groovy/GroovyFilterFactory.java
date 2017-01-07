package be.echostyle.moola.filters.groovy;

import be.echostyle.moola.filters.FilterExpressionException;
import be.echostyle.moola.filters.TransactionFilter;
import be.echostyle.moola.filters.TransactionFilterFactory;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyFilterFactory implements TransactionFilterFactory {

    private static final Logger log = LoggerFactory.getLogger(GroovyFilterFactory.class);

    private final GroovyShell shell;

    public GroovyFilterFactory(){
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(GroovyTransactionFilter.class.getCanonicalName());
        shell = new GroovyShell(cc);
    }

    @Override
    public TransactionFilter createFilter(String filterExpression) {
        if (filterExpression.contains("targetEntry")) throw new FilterExpressionException(filterExpression);
        try {
            GroovyTransactionFilter r = (GroovyTransactionFilter) shell.parse(filterExpression);
            r.setFilterExpression(filterExpression);
            return r;
        } catch (CompilationFailedException e){
            log.debug("Compilation failed on "+filterExpression, e);
            throw new FilterExpressionException(filterExpression);
        }
    }
}
