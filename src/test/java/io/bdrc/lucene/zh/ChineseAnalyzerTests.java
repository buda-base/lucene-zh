package io.bdrc.lucene.zh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class ChineseAnalyzerTests {    
    public static List<String> parseTokens(Analyzer analyzer, String input) throws IOException {

        List<String> result = new ArrayList<String>();
        TokenStream stream  = analyzer.tokenStream(null, new StringReader(input));
        stream.reset();
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        
        try {
            while(stream.incrementToken()) {
                result.add(charTermAttribute.toString());
            }
        }
        catch(IOException e) {
            // not thrown b/c we're using a string reader...
        }

        return result;
    }  
    
    @Test
    public void testTC2PYstrict() throws IOException
    {
        // https://github.com/axgle/pinyin/blob/master/pinyin_test.go
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        List<String> expected = Arrays.asList("shì", "jiè", "wén", "hàn", "zì", 
                "pīn", "yīn", "jiǎn", "tǐ", "zì", "guǎn", "pú", "lú", "luò", "bó", "dān");
        System.out.println("0 " + input);
        Analyzer ca = new ChineseAnalyzer("TC2PYstrict");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testTC2PYlazy() throws IOException
    {
        // https://github.com/axgle/pinyin/blob/master/pinyin_test.go
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        List<String> expected = Arrays.asList("shi", "jie", "wen", "han", "zi", 
                "pin", "yin", "jian", "ti", "zi", "guan", "pu", "lu", "luo", "bo", "dan");
        System.out.println("0 " + input);
        Analyzer ca = new ChineseAnalyzer("TC2PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
}
