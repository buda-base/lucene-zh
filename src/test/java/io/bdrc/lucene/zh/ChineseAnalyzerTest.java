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

public class ChineseAnalyzerTest {    
    
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
    public void testTCbis() throws IOException
    {
        String input = "如是我聞。一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱";
// With and without stopwords (no variants).
//      聞   時佛  羅閱祇耆闍崛山   與   丘眾千  百 十  菩薩  千 俱
// 如是我聞。一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱
        List<String> expected = Arrays.asList("聞", "時", "佛", "羅", "閱", "祇", "耆", "闍", 
                "崛", "山", "與", "丘", "眾", "千", "百", "十", "菩", "薩", "千", "俱");
        Analyzer ca = new ChineseAnalyzer("TC", true, 0);
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testTC2PYstrict() throws IOException
    {
        String input = "如是我聞。一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱";
        List<String> expected = Arrays.asList("wén", "shí", "fó", "luō", "yuè", "qí", "qí", 
                "dū", "jué", "shān", "yǔ", "qiū", "zhòng", "qiān", "bǎi", "shí", "pú", "sà", 
                "qiān", "jù");
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
// The second line shows the output with no variant applied, keeping all stopwords conf: ChineseAnalyzer("TC2PYlazy", false, 0) 
//              wen,     shi, fo,      luo, yue, qi, qi, du, jue, shan,        yu,         qiu, zhong, qian,     bai,     shi,      pu, sa,     qian,      ju
// ru, shi, wo, wen, yi, shi, fu, zai, luo, yue, qi, qi, du, jue, shan, zhong, yu, da, bi, qiu, zhong, qian, er, bai, wu, shi, ren, pu, sa, wu, qian, ren, ju
        String input = "如是我聞。一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱";
        List<String> expected = Arrays.asList("wen", "shi", "fo", "luo", "yue", "qi", "qi", 
                "du", "jue", "shan", "yu", "qiu", "zhong", "qian", "bai", "shi", "pu", "sa", 
                "qian", "ju");
        Analyzer ca = new ChineseAnalyzer("TC2PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testTC2PYlazyNoStopsNoVariants() throws IOException
    {
        String input = "如是我聞。一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱";
        List<String> expected = Arrays.asList("ru", "shi", "wo", "wen", "yi", "shi", "fu", 
                "zai", "luo", "yue", "qi", "qi", "du", "jue", "shan", "zhong", "yu", "da", 
                "bi", "qiu", "zhong", "qian", "er", "bai", "wu", "shi", "ren", "pu", "sa", 
                "wu", "qian", "ren", "ju");
        Analyzer ca = new ChineseAnalyzer("TC2PYlazy", false, 0);
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testPYstrict2PYlazy() throws IOException
    {
        String input = "Rúshì wǒwén. Yī shí fú zài luó yuè qí qí dū jué shān zhōng. "
                + "Yǔ dà bǐ qiū zhòng qiān èr bǎi wǔ shí rén pú sà wǔ qiān rén jù.";
        List<String> expected = Arrays.asList("ru", "shi", "wo", "wen", "yi", "shi", "fu", 
                "zai", "luo", "yue", "qi", "qi", "du", "jue", "shan", "zhong", "yu", "da", 
                "bi", "qiu", "zhong", "qian", "er", "bai", "wu", "shi", "ren", "pu", "sa", 
                "wu", "qian", "ren", "ju");
        Analyzer ca = new ChineseAnalyzer("PYstrict2PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testPYmarked() throws IOException
    {
        // strictly speaking, capitals are not allowed in Pinyin except at initials of words
        // we tolerate them.
        String input = "Rú sHì Wǒ wéN. Yī shí fú zài luó yuè qí qí dū jué shān zhōng. "
                + "Yǔ dà bǐ qiū zhòng qiān èr bǎi wǔ shí rén pú sà wǔ qiān rén jù.";
        List<String> expected = Arrays.asList("rú", "shì", "wǒ", "wén", "yī", "shí", "fú", 
                "zài", "luó", "yuè", "qí", "qí", "dū", "jué", "shān", "zhōng", "yǔ", "dà", 
                "bǐ", "qiū", "zhòng", "qiān", "èr", "bǎi", "wǔ", "shí", "rén", "pú", "sà", 
                "wǔ", "qiān", "rén", "jù");
        Analyzer ca = new ChineseAnalyzer("PYstrict");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testPYlazy() throws IOException
    {
        String input = "RusHi  Wowen. Yi shi fu zai luo yue qi qi du jue shan zhong. "
                + "Yu da bi qiu zhong qian er bai wu shi ren pu sa wu qian ren ju.";
        List<String> expected = Arrays.asList("ru", "shi", "wo", "wen", "yi", "shi", "fu", 
                "zai", "luo", "yue", "qi", "qi", "du", "jue", "shan", "zhong", "yu", "da", 
                "bi", "qiu", "zhong", "qian", "er", "bai", "wu", "shi", "ren", "pu", "sa", 
                "wu", "qian", "ren", "ju");
        Analyzer ca = new ChineseAnalyzer("PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testPYstrictToPYlazy() throws IOException
    {
        // r3 is a non-word. it will be tokenized into 'r' + '3'. numbers in non-valid syllables are left intact.
        String input = "Rú sHì Wǒ wéN. +@/* r3 yi0 yi5 miao1 fei1 zhou3 huo3 lün1 lvn2 yi1 wan4 nian2 jing1";
        List<String> expected = Arrays.asList("ru", "shi", "wo", "wen", "r", "3", "yi", "yi", "miao", "fei", 
                "zhou", "huo", "lun", "lun", "yi", "wan", "nian", "jing");
        Analyzer ca = new ChineseAnalyzer("PYstrict2PYlazy");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testPYstrict() throws IOException
    {
        String input = "Rú sHì Wǒ wéN yi0 yi5 miao1";
        List<String> expected = Arrays.asList("rú", "shì", "wǒ", "wén", "yi", "yi", "miāo");
        Analyzer ca = new ChineseAnalyzer("PYstrict");
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }
    
    @Test
    public void testTC2PyStrict() throws IOException
    {
        String input = "丹 珠 尔";
        List<String> expected = Arrays.asList("dān", "zhū", "ěr");
        Analyzer ca = new ChineseAnalyzer("TC2PYstrict", false, 0);
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        assertEquals(expected, tokens);
    }

    // TODO: fix: doesn't work because 经 is a stopword 
    @Test
    public void testTC2SC() throws IOException
    {
        String input = "經";
        List<String> expected = Arrays.asList("经");
        Analyzer ca = new ChineseAnalyzer("TC2SC", true, 3);
        List<String> tokens = parseTokens(ca, input);
        System.out.println("1 " + expected.toString());
        System.out.println("2 " + tokens.toString());
        System.out.println();
        //assertEquals(expected, tokens);
    }
}
