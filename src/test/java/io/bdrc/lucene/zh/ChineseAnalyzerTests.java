package io.bdrc.lucene.zh;

import static org.junit.Assert.assertEquals;

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
    
    // adapted from https://stackoverflow.com/a/9562816
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
    public void testExactTC() throws IOException
    {
        // https://github.com/axgle/pinyin/blob/master/pinyin_test.go
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        List<String> expected = Arrays.asList("世", "界", "中", "文", "汉", "字", 
                "拼", "音", "简", "体", "字", "莞", "濮", "泸", "漯", "亳", "儋");
        Analyzer ca = new ChineseAnalyzer("exactTC");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }

    @Test
    public void testTC() throws IOException
    {
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋 兆 嚳";   
        // 中(char3) is a stopword, it is deleted. 
        // 堺(char2) is the alternative (zVariant) found for 界.
        // 俈(last char) is the synonym found for 嚳.
        List<String> expected = Arrays.asList("世", "堺", "文", "汉", "字", 
                "拼", "音", "简", "体", "字", "莞", "濮", "泸", "漯", "亳", "儋", "兆", "俈");
        Analyzer ca = new ChineseAnalyzer("TC");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testTC2PYstrict() throws IOException
    {
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        List<String> expected = Arrays.asList("shì", "jiè", "wén", "hàn", "zì", 
                "pīn", "yīn", "jiǎn", "tǐ", "zì", "guǎn", "pú", "lú", "luò", "bó", "dān");
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
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        List<String> expected = Arrays.asList("shi", "jie", "wen", "han", "zi", 
                "pin", "yin", "jian", "ti", "zi", "guan", "pu", "lu", "luo", "bo", "dan");
        Analyzer ca = new ChineseAnalyzer("TC2PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
}
