package org.supercsv.ext.builder.joda;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Optional;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.joda.FmtLocalDateTime;
import org.supercsv.cellprocessor.joda.ParseLocalDateTime;
import org.supercsv.ext.annotation.CsvDateConverter;
import org.supercsv.ext.exception.SuperCsvInvalidAnnotationException;

/**
 * The cell processor builder for {@link LocalDateTime} with Joda-Time.
 * 
 * @since 1.2
 * @author T.TSUCHIE
 *
 */
public class LocalDateTimeCellProcessorBuilder extends AbstractJodaCellProcessorBuilder<LocalDateTime> {
    
    @Override
    protected String getDefaultPattern() {
        return "yyyy/MM/dd HH:mm:ss";
    }
    
    @Override
    protected LocalDateTime parseJoda(final String value, final DateTimeFormatter formatter) {
        return formatter.parseLocalDateTime(value);
    }
    
    @Override
    public LocalDateTime getParseValue(final Class<LocalDateTime> type, final Annotation[] annos, final String strValue) {
        
        final Optional<CsvDateConverter> converterAnno = getAnnotation(annos);
        
        final String pattern = getPattern(converterAnno);
        final Locale locale = getLocale(converterAnno);
        final DateTimeZone zone = getDateTimeZone(converterAnno);
        
        final DateTimeFormatter formatter = createDateTimeFormatter(pattern, locale, zone);
        
        try {
            return LocalDateTime.parse(strValue, formatter);
            
        } catch(IllegalArgumentException e) {
            throw new SuperCsvInvalidAnnotationException(
                    String.format("default '%s' value cannot parse to LocalDateTime with pattern '%s'",
                            strValue, pattern), e);
            
        }
    }
    
    @Override
    public CellProcessor buildOutputCellProcessor(final Class<LocalDateTime> type,final  Annotation[] annos,
            final CellProcessor processor, final boolean ignoreValidationProcessor) {
        
        final Optional<CsvDateConverter> converterAnno = getAnnotation(annos);
        final String pattern = getPattern(converterAnno);
        final Locale locale = getLocale(converterAnno);
        final DateTimeZone zone = getDateTimeZone(converterAnno);
        
        final DateTimeFormatter formatter = createDateTimeFormatter(pattern, locale, zone);
        
        final Optional<LocalDateTime> min = getMin(converterAnno).map(s -> parseJoda(s, formatter));
        final Optional<LocalDateTime> max = getMax(converterAnno).map(s -> parseJoda(s, formatter));
        
        CellProcessor cp = processor;
        cp = (cp == null ? new FmtLocalDateTime(formatter) : new FmtLocalDateTime(formatter, cp));
        
        if(!ignoreValidationProcessor) {
            cp = prependRangeProcessor(min, max, cp);
        }
        
        return cp;
        
    }
    
    @Override
    public CellProcessor buildInputCellProcessor(final Class<LocalDateTime> type, final Annotation[] annos,
            final CellProcessor processor) {
        
        final Optional<CsvDateConverter> converterAnno = getAnnotation(annos);
        final String pattern = getPattern(converterAnno);
        final Locale locale = getLocale(converterAnno);
        final DateTimeZone zone = getDateTimeZone(converterAnno);
        
        final DateTimeFormatter formatter = createDateTimeFormatter(pattern, locale, zone);
        
        final Optional<LocalDateTime> min = getMin(converterAnno).map(s -> parseJoda(s, formatter));
        final Optional<LocalDateTime> max = getMax(converterAnno).map(s -> parseJoda(s, formatter));
        
        CellProcessor cp = processor;
        cp = prependRangeProcessor(min, max, cp);
        cp = (cp == null ? new ParseLocalDateTime(formatter) : new ParseLocalDateTime(formatter, cp));
        
        return cp;
    }
    
    
}
