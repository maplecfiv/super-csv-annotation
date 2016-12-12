package com.github.mygreen.supercsv.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.mygreen.supercsv.exception.SuperCsvNoMatchHeaderException;
import com.github.mygreen.supercsv.io.CsvAnnotationBeanReader;
import com.github.mygreen.supercsv.io.CsvAnnotationBeanWriter;
import com.github.mygreen.supercsv.validation.CsvValidator;


/**
 * CSV用のBeanであることを表現するアノテーションです。
 * <p>クラスに付与します。</p>
 * 
 * @version 2.0
 * @author T.TSUCHIE
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvBean {
    
    /**
     * ヘッダーが存在することを前提として処理します。
     * <p>{@link CsvAnnotationBeanReader#readAll(boolean)}、{@link CsvAnnotationBeanWriter#writeAll(java.util.Collection, boolean)}を呼び出した際に利用します。</p>
     * @return trueの場合、読み込み時には1行目はヘッダー情報として読み込み、書き出し時にはヘッダー行が出力されるようになります。
     */
    boolean header() default false;
    
    /**
     * ヘッダー行の読み込み時に、値の検証を行うか指定します。
     * <p>部分的にカラムの読み込みを行うときは、falseに設定してください。</p>
     * 
     * @since 2.0
     * @return trueの場合、ヘッダー行の値が定義されている値と同じかどうか検証を行います。
     *         ヘッダーの値が不正な場合、例外{@link SuperCsvNoMatchHeaderException}がスローされます。
     */
    boolean validateHeader() default false;
    
    /**
     * レコードに対する値の検証を行うクラスを指定します。
     * <p>カラム間の相関チェックやBeanValidationを使用する際に指定します。</p>
     * 
     * @since 2.0
     * @return {@link CsvValidator}を実装したクラスを指定します。
     *         複数指定可能で、指定した順に実行されます。
     * 
     */
    Class<? extends CsvValidator<?>>[] validators() default {};
    
    /**
     * ライフサイクルコールバック用のリスナークラスを指定するためのアノテーション。
     * @since 2.0
     * @return コールバック用アノテーションを指定したメソッドを指定します。
     */
    Class<?>[] listeners() default {};
    
}
