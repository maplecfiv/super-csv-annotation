package org.supercsv.ext.builder.impl;

import static org.junit.Assert.*;
import static org.supercsv.ext.tool.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.supercsv.ext.tool.HasCellProcessor.*;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.Equals;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.Unique;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.ext.annotation.CsvBean;
import org.supercsv.ext.annotation.CsvColumn;
import org.supercsv.ext.annotation.CsvDateConverter;
import org.supercsv.ext.cellprocessor.FormatLocaleDate;
import org.supercsv.ext.cellprocessor.ParseLocaleDate;
import org.supercsv.ext.cellprocessor.Trim;
import org.supercsv.ext.cellprocessor.constraint.DateRange;
import org.supercsv.ext.cellprocessor.constraint.FutureDate;
import org.supercsv.ext.cellprocessor.constraint.PastDate;
import org.supercsv.ext.exception.SuperCsvInvalidAnnotationException;

/**
 * Test the {@link DateCellProcessorBuilder} CellProcessor.
 * 
 * @since 1.2
 * @author T.TSUCHIE
 *
 */
public class DateCellProcessorBuilderTest {
    
    @Rule
    public TestName name = new TestName();
    
    private DateCellProcessorBuilder builder;
    
    /**
     * Sets up the processor for the test using Combinations
     */
    @Before
    public void setUp() {
        builder = new DateCellProcessorBuilder();
    }
    
    private static final String TEST_NORMAL_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TEST_FORMATTED_PATTERN = "yy/M/d H:m:s";
    
    private static final Date TEST_VALUE_1_OBJ = toDate(2016, 2, 29, 7, 12, 1);
    private static final String TEST_VALUE_1_STR_NORMAL = "2016-02-29 07:12:01";
    private static final String TEST_VALUE_1_STR_FORMATTED = "16/2/29 7:12:1";
    
    private static final Date TEST_VALUE_2_OBJ = toDate(2020, 1, 31, 10, 8, 35);
    private static final String TEST_VALUE_2_STR_NORMAL = "2020-01-31 10:08:35";
    private static final String TEST_VALUE_2_STR_FORMATTED = "20/1/31 10:8:35";
    
    private static final Date TEST_VALUE_INPUT_DEFAULT_OBJ = toDate(2000, 1, 1, 8, 2, 3);
    private static final String TEST_VALUE_INPUT_DEFAULT_STR_NORMAL = "2000-01-01 08:02:03";
    private static final String TEST_VALUE_INPUT_DEFAULT_STR_FORMATTED = "00/1/1 8:2:3";
    
    private static final Date TEST_VALUE_OUTPUT_DEFAULT_OBJ = toDate(2015, 12, 31, 12, 31, 1);
    private static final String TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL = "2015-12-31 12:31:01";
    private static final String TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED = "15/12/31 12:31:1";
    
    private static final Date TEST_VALUE_MIN_OBJ = toDate(2000, 1, 1, 1, 0, 0);
    private static final String TEST_VALUE_MIN_STR_NORMAL = "2000-01-01 01:00:00";
    private static final String TEST_VALUE_MIN_STR_FORMATTED = "00/1/1 1:0:0";
    
    private static final Date TEST_VALUE_MAX_OBJ = toDate(2010, 12, 31, 4, 59, 59);
    private static final String TEST_VALUE_MAX_STR_NORMAL = "2010-12-31 04:59:59";
    private static final String TEST_VALUE_MAX_STR_FORMATTED = "10/12/31 4:59:59";
    
    @CsvBean
    private static class TestCsv {
        
        @CsvColumn(position=0)
        Date date_default;
        
        @CsvColumn(position=1, optional=true)
        Date date_optional;
        
        @CsvColumn(position=2, trim=true)
        Date date_trim;
        
        @CsvColumn(position=3, inputDefaultValue=TEST_VALUE_INPUT_DEFAULT_STR_NORMAL, outputDefaultValue=TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL)
        Date date_defaultValue;
        
        @CsvColumn(position=4, inputDefaultValue=TEST_VALUE_INPUT_DEFAULT_STR_FORMATTED, outputDefaultValue=TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED)
        @CsvDateConverter(pattern=TEST_FORMATTED_PATTERN)
        Date date_defaultValue_format;
        
        @CsvColumn(position=4, inputDefaultValue="2000-01-01 08:02:03")
        @CsvDateConverter(pattern=TEST_FORMATTED_PATTERN)
        Date date_defaultValue_format_invalid;
        
        @CsvColumn(position=5, equalsValue=TEST_VALUE_1_STR_NORMAL)
        Date date_equalsValue;
        
        @CsvColumn(position=6, equalsValue=TEST_VALUE_1_STR_FORMATTED)
        @CsvDateConverter(pattern=TEST_FORMATTED_PATTERN)
        Date date_equalsValue_format;
        
        @CsvColumn(position=7, unique=true)
        Date date_unique;
        
        @CsvColumn(position=8, unique=true)
        @CsvDateConverter(pattern=TEST_FORMATTED_PATTERN)
        Date date_unique_format;
        
        @CsvColumn(position=9, optional=true, trim=true,
                inputDefaultValue=TEST_VALUE_INPUT_DEFAULT_STR_NORMAL, outputDefaultValue=TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL,
                equalsValue=TEST_VALUE_1_STR_NORMAL, unique=true)
        Date date_combine1;
        
        @CsvColumn(position=10, optional=true, trim=true,
                inputDefaultValue=TEST_VALUE_INPUT_DEFAULT_STR_FORMATTED, outputDefaultValue=TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED,
                equalsValue=TEST_VALUE_1_STR_FORMATTED, unique=true)
        @CsvDateConverter(pattern=TEST_FORMATTED_PATTERN)
        Date date_combine_format1;
        
        @CsvColumn(position=11)
        @CsvDateConverter(min=TEST_VALUE_MIN_STR_NORMAL)
        Date date_min;
        
        @CsvColumn(position=12)
        @CsvDateConverter(min=TEST_VALUE_MIN_STR_FORMATTED, pattern=TEST_FORMATTED_PATTERN)
        Date date_min_format;
        
        @CsvColumn(position=13)
        @CsvDateConverter(max=TEST_VALUE_MAX_STR_NORMAL)
        Date date_max;
        
        @CsvColumn(position=14)
        @CsvDateConverter(max=TEST_VALUE_MAX_STR_FORMATTED, pattern=TEST_FORMATTED_PATTERN)
        Date date_max_format;
        
        @CsvColumn(position=15)
        @CsvDateConverter(min=TEST_VALUE_MIN_STR_NORMAL, max=TEST_VALUE_MAX_STR_NORMAL)
        Date date_range;
        
        @CsvColumn(position=16)
        @CsvDateConverter(min=TEST_VALUE_MIN_STR_FORMATTED, max=TEST_VALUE_MAX_STR_FORMATTED, pattern=TEST_FORMATTED_PATTERN)
        Date date_range_format;
        
        @CsvColumn(position=17)
        @CsvDateConverter(lenient=false)
        Date date_lenient;
        
        @CsvColumn(position=17)
        @CsvDateConverter(pattern="GGGGyy年M月d日 H時m分s秒", locale="ja_JP_JP")
        Date date_locale;
        
        @CsvColumn(position=17)
        @CsvDateConverter(timezone="GMT")
        Date date_timezone;
    }
    
    /**
     * Tests with default. (not grant convert annotation.)
     */
    @Test
    public void testBuildInput_default() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_default");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(NotNull.class));
        assertThat(cellProcessor, hasCellProcessor(ParseLocaleDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // null input
        try {
            cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(NotNull.class)));
        }
        
        // wrong pattern
        try {
            cellProcessor.execute("abc", ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvCellProcessorException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(ParseLocaleDate.class)));
        }
        
    }
    
    /**
     * Tests with default. (not grant convert annotation.)
     */
    @Test
    public void testBuildOutput_default() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_default");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(NotNull.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // null input
        try {
            cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(NotNull.class)));
        }
        
    }
    
    /**
     * Tests with default. (not grant convert annotation.)
     */
    @Test
    public void testBuildOutput_default_ignoreValidation() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_default");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(NotNull.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // null input
        try {
            cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(NotNull.class)));
        }
    }
    
    /**
     * Tests with optional. (not grant convert annotation.)
     */
    @Test
    public void testBuildInput_optional() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_optional");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Optional.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // null input
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(nullValue()));
    }
    
    /**
     * Tests with optional. (not grant convert annotation.)
     */
    @Test
    public void testBuildOutput_optional() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_optional");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Optional.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // null input
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(nullValue()));
    }
    
    /**
     * Tests with optional. (not grant convert annotation.)
     */
    @Test
    public void testBuildOutput_optional_ignoreValidation() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_optional");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Optional.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // null input
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(nullValue()));
    }
    
    @Test
    public void testBuildInput_trim() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_trim");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Trim.class));
        
        assertThat(cellProcessor.execute("  " + TEST_VALUE_1_STR_NORMAL + " ", ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
    }
    
    @Test
    public void testBuildOutput_trim() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_trim");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Trim.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
    }
    
    @Test
    public void testBuildOutput_trim_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_trim");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Trim.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
    }
    
    @Test
    public void testBuildInput_defaultValue() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_INPUT_DEFAULT_OBJ));
    }
    
    @Test
    public void testBuildOutput_defaultValue() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL));
    }
    
    @Test
    public void testBuildOutput_defaultValue_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL));
    }
    
    @Test
    public void testBuildInput_defaultValue_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_INPUT_DEFAULT_OBJ));
    }
    
    @Test
    public void testBuildOutput_defaultValue_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED));
    }
    
    @Test
    public void testBuildOutput_defaultValue_format_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ConvertNullTo.class));
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED));
    }
    
    /**
     * Tests with default. (not grant convert annotation.)
     */
    @Test(expected=SuperCsvInvalidAnnotationException.class)
    public void testBuildInput_default_format_invalidAnnotation() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_defaultValue_format_invalid");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        
        cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT);
        fail();
        
    }
    
    @Test
    public void testBuildInput_equalsValue() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Equals.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_2_STR_NORMAL, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
    }
    
    @Test
    public void testBuildOutput_equalsValue() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        assertThat(cellProcessor, hasCellProcessor(Equals.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
    }
    
    @Test
    public void testBuildOutput_equalsValue_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(Equals.class)));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not quals input
        assertThat(cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_2_STR_NORMAL));
        
    }
    
    @Test
    public void testBuildInput_equalsValue_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Equals.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_2_STR_FORMATTED, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
    }
    
    @Test
    public void testBuildOutput_equalsValue_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Equals.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
    }
    
    @Test
    public void testBuildOutput_equalsValue_format_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_equalsValue_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(Equals.class)));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not quals input
        assertThat(cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_2_STR_FORMATTED));
        
    }
    
    @Test
    public void testBuildInput_unique() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Unique.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_unique() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Unique.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_unique_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(Unique.class)));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not quals input
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
    }
    
    @Test
    public void testBuildInput_unique_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Unique.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_1_STR_FORMATTED, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_unique_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(Unique.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not quals input
        try {
            cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_unique_format_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_unique_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(Unique.class)));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not quals input
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
    }
    
    @Test
    public void testBuildInput_combine1() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine1");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_INPUT_DEFAULT_OBJ));
        assertThat(cellProcessor.execute("   " + TEST_VALUE_1_STR_NORMAL + "   ", ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not equals input
        try {
            cellProcessor.execute(TEST_VALUE_2_STR_NORMAL, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_combine1() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine1");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL));
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not equals input
        try {
            cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_combine1_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine1");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_NORMAL));
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
        // not equals input
        assertThat(cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_2_STR_NORMAL));
        
        // not unique input
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
    }
    
    @Test
    public void testBuildInput_format_combine1() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine_format1");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_INPUT_DEFAULT_OBJ));
        assertThat(cellProcessor.execute("   " + TEST_VALUE_1_STR_FORMATTED + "   ", ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        // not equals input
        try {
            cellProcessor.execute(TEST_VALUE_2_STR_FORMATTED, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_STR_FORMATTED, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_format_combine1() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine_format1");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED));
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not equals input
        try {
            cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Equals.class)));
        }
        
        // not unique input
        try {
            cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(Unique.class)));
        }
    }
    
    @Test
    public void testBuildOutput_combine_format1_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_combine_format1");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor.execute(null, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_OUTPUT_DEFAULT_STR_FORMATTED));
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
        // not equals input
        assertThat(cellProcessor.execute(TEST_VALUE_2_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_2_STR_FORMATTED));
        
        // not unique input
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_FORMATTED));
        
    }
    
    @Test
    public void testBuildInput_min() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FutureDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_OBJ));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(FutureDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_min() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FutureDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_STR_NORMAL));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // less min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(FutureDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_min_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(FutureDate.class)));
        
        // less than min value
        {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_min_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FutureDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_OBJ));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(FutureDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_min_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FutureDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_STR_FORMATTED));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(FutureDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_min_format_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_min_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(FutureDate.class)));
        
        // less than min value
        {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_max() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(PastDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_OBJ));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(PastDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_max() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(PastDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_STR_NORMAL));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(PastDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_max_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(PastDate.class)));
        
        // greater than max value
        {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_max_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(PastDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_OBJ));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(PastDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_max_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(PastDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_STR_FORMATTED));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(PastDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_max_format_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_max_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(PastDate.class)));
        
        // greater than max value
        {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_range() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(DateRange.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_OBJ));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_OBJ));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_range() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(DateRange.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_STR_NORMAL));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_STR_NORMAL));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_range_ignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(DateRange.class)));
        
        // less than min value
        {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_NORMAL_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_range_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range_format");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(DateRange.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_OBJ));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_STR_FORMATTED, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_OBJ));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT), is(obj));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(str, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_range_format() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(DateRange.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_MIN_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MIN_STR_FORMATTED));
        
        // greater than min value
        {
            Date obj = plusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // less than min value
        try {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
        assertThat(cellProcessor.execute(TEST_VALUE_MAX_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_MAX_STR_FORMATTED));
        
        // less than max value
        {
            Date obj = minusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        try {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT);
            fail();
        } catch(SuperCsvConstraintViolationException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(DateRange.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_range_formatignoreValidation() {
        Annotation[] annos = getAnnotations(TestCsv.class, "date_range_format");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, true);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, not(hasCellProcessor(DateRange.class)));
        
        // less than min value
        {
            Date obj = minusSeconds(TEST_VALUE_MIN_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
        
        // greater than max value
        {
            Date obj = plusSeconds(TEST_VALUE_MAX_OBJ, 1);
            String str = format(obj, TEST_FORMATTED_PATTERN);
            
            assertThat(cellProcessor.execute(obj, ANONYMOUS_CSVCONTEXT), is(str));
        }
    }
    
    @Test
    public void testBuildInput_lenient() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_lenient");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ParseLocaleDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
        try {
            cellProcessor.execute("2016-02-31 07:12:01", ANONYMOUS_CSVCONTEXT);
            fail();
            
        } catch(SuperCsvCellProcessorException e) {
            CellProcessor errorProcessor = e.getProcessor();
            assertThat(errorProcessor, is(instanceOf(ParseLocaleDate.class)));
        }
        
    }
    
    @Test
    public void testBuildOutput_lenient() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_lenient");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FormatLocaleDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_STR_NORMAL));
        
    }
    
    @Test
    public void testBuildInput_locale() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_locale");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ParseLocaleDate.class));
        
        assertThat(cellProcessor.execute("平成28年2月29日 7時12分1秒", ANONYMOUS_CSVCONTEXT), is(TEST_VALUE_1_OBJ));
        
    }
    
    @Test
    public void testBuildOutput_locale() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_locale");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FormatLocaleDate.class));
        
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is("平成28年2月29日 7時12分1秒"));
        
    }
    
    @Test
    public void testBuildInput_timezone() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_timezone");
        CellProcessor cellProcessor = builder.buildInputCellProcessor(Date.class, annos);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(ParseLocaleDate.class));
        
        TimeZone tz = TimeZone.getDefault();
        Date expected = plusHours(TEST_VALUE_1_OBJ, (int)TimeUnit.MILLISECONDS.toHours(tz.getRawOffset()));
        assertThat(cellProcessor.execute(TEST_VALUE_1_STR_NORMAL, ANONYMOUS_CSVCONTEXT), is(expected));
        
    }
    
    @Test
    public void testBuildOutput_timezone() {
        
        Annotation[] annos = getAnnotations(TestCsv.class, "date_timezone");
        CellProcessor cellProcessor = builder.buildOutputCellProcessor(Date.class, annos, false);
        printCellProcessorChain(cellProcessor, name.getMethodName());
        
        assertThat(cellProcessor, hasCellProcessor(FormatLocaleDate.class));
        
        TimeZone tz = TimeZone.getDefault();
        String expected = format(minusHours(TEST_VALUE_1_OBJ, (int)TimeUnit.MILLISECONDS.toHours(tz.getRawOffset())), TEST_NORMAL_PATTERN);
        assertThat(cellProcessor.execute(TEST_VALUE_1_OBJ, ANONYMOUS_CSVCONTEXT), is(expected));
        
    }
    
}
