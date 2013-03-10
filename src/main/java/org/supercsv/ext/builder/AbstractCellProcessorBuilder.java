/*
 * AbstractCellProcessorBuilder.java
 * created in 2013/03/05
 *
 * (C) Copyright 2003-2013 GreenDay Project. All rights reserved.
 */
package org.supercsv.ext.builder;

import java.lang.annotation.Annotation;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.Equals;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.ext.annotation.CsvColumn;


/**
 *
 *
 * @author T.TSUCHIE
 *
 */
public abstract class AbstractCellProcessorBuilder<T> {
    
    protected CsvColumn getCsvColumnAnnotation(final Annotation[] annos) {
        
        for(Annotation anno : annos) {
            if(anno instanceof CsvColumn) {
                return (CsvColumn) anno;
            }
        }
        
        return null;
        
    }
    
    public CellProcessor buildOutputCellProcessor(final Class<T> type, final Annotation[] annos, final boolean ignoreValidableProcessor) {
        
        CsvColumn csvColumnAnno = getCsvColumnAnnotation(annos);
        
        CellProcessor cellProcessor = null;
        
        if(csvColumnAnno.trim()) {
            cellProcessor = prependTrimProcessor(cellProcessor);
        }
        
        cellProcessor = buildOutputCellProcessor(type, annos, cellProcessor, ignoreValidableProcessor);
        
        if(csvColumnAnno.unique() && !ignoreValidableProcessor) {
            cellProcessor = prependUniqueProcessor(cellProcessor);
        }
        
        if(!csvColumnAnno.equalsValue().isEmpty() && !ignoreValidableProcessor) {
            cellProcessor = prependEqualsProcessor(type, cellProcessor,
                    getParseValue(type, annos, csvColumnAnno.equalsValue()));
        }
        
        if(!csvColumnAnno.outputDefaultValue().isEmpty()) {
            cellProcessor = prependConvertNullToProcessor(type, cellProcessor,
                    getParseValue(type, annos, csvColumnAnno.outputDefaultValue()));
        }
        
        if(csvColumnAnno.optional() && !type.isPrimitive()) {
            cellProcessor = prependOptionalProcessor(cellProcessor);
        } else {
            cellProcessor = prependNotNullProcessor(cellProcessor);
        }
        
        return cellProcessor;
    }
    
    public CellProcessor buildInputCellProcessor(final Class<T> type, final Annotation[] annos) {
        
        CsvColumn csvColumnAnno = getCsvColumnAnnotation(annos);
        
        CellProcessor cellProcessor = null;
        cellProcessor = buildInputCellProcessor(type, annos, cellProcessor);
        
        if(csvColumnAnno.unique()) {
            cellProcessor = prependUniqueProcessor(cellProcessor);
        }
        
        if(!csvColumnAnno.equalsValue().isEmpty()) {
            cellProcessor = prependEqualsProcessor(type, cellProcessor,
                    getParseValue(type, annos, csvColumnAnno.equalsValue()));
        }
        
        if(csvColumnAnno.trim()) {
            cellProcessor = prependTrimProcessor(cellProcessor);
        }
        
        if(csvColumnAnno.optional() && !type.isPrimitive()) {
            cellProcessor = prependOptionalProcessor(cellProcessor);
        } else {
            cellProcessor = prependNotNullProcessor(cellProcessor);
        }
        
        if(!csvColumnAnno.inputDefaultValue().isEmpty()) {
            cellProcessor = prependConvertNullToProcessor(type, cellProcessor,
                    getParseValue(type, annos, csvColumnAnno.inputDefaultValue()));
        }
        
        return cellProcessor;
    }
    
    protected CellProcessor prependConvertNullToProcessor(final Class<T> type, final CellProcessor cellProcessor, final Object value) {
        
        return (cellProcessor == null ? 
                new ConvertNullTo(value) : new ConvertNullTo(value, cellProcessor));
    }
    
    protected CellProcessor prependEqualsProcessor(final Class<T> type, final CellProcessor cellProcessor, final Object value) {
        
        return (cellProcessor == null ? 
                new Equals(value) : new Equals(value, cellProcessor));
    }
    
    protected CellProcessor prependUniqueProcessor(final CellProcessor cellProcessor) {
        return (cellProcessor == null ? new Unique() : new Unique(cellProcessor));
    }
    
    protected CellProcessor prependOptionalProcessor(final CellProcessor cellProcessor) {
        return (cellProcessor == null ? new Optional() : new Optional(cellProcessor));
    }
    
    protected CellProcessor prependNotNullProcessor(final CellProcessor cellProcessor) {
        return (cellProcessor == null ? new NotNull() : new NotNull(cellProcessor));
    }
    
    protected CellProcessor prependTrimProcessor(final CellProcessor cellProcessor) {
        return (cellProcessor == null ? new Trim() : new Trim((StringCellProcessor) cellProcessor));
    }
    
    public abstract CellProcessor buildOutputCellProcessor(Class<T> type, Annotation[] annos, CellProcessor cellProcessor, boolean ignoreValidableProcessor);
    
    public abstract CellProcessor buildInputCellProcessor(Class<T> type, Annotation[] annos, CellProcessor cellProcessor);
    
    public abstract T getParseValue(Class<T> type, Annotation[] annos, String strValue);
    
}
