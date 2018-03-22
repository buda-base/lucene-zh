/*******************************************************************************
 * Copyright (c) 2018 Buddhist Digital Resource Center (BDRC)
 * 
 * If this file is a derivation of another work the license header will appear 
 * below; otherwise, this work is licensed under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with the 
 * License.
 * 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package io.bdrc.lucene.zh;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

/**
 * Unit tests for the Chinese tokenizers and filters.
 * 
 * @author Hélios Hildt
 */
public class ChineseComponentsTests  {

    static TokenStream tokenize(Reader reader, Tokenizer tokenizer) throws IOException {
        tokenizer.close();
        tokenizer.end();
        tokenizer.setReader(reader);
        tokenizer.reset();
        return tokenizer;
    }
    
    static private void assertCharTokenStream(TokenStream tokenStream, List<String> expected) {
        try {
            List<String> termList = new ArrayList<String>();
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                termList.add(charTermAttribute.toString() + charTermAttribute.length());
            }
            System.out.println("1 " + String.join(" ", expected));
            System.out.println("2 " + String.join(" ", termList) + "\n");
            assertThat(termList, is(expected));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
    
    static private void assertTokenStream(TokenStream tokenStream, List<String> expected) {
        try {
            List<String> termList = new ArrayList<String>();
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                termList.add(charTermAttribute.toString());
            }
            System.out.println("1 " + String.join(" ", expected));
            System.out.println("2 " + String.join(" ", termList) + "\n");
            assertThat(termList, is(expected));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
    
    @Test
    public void testChineseIndexingAndCodepointLength() throws IOException
    {
        String input = "𪘁! this is a test. 如是我聞。japanese: ひらがな 一時佛在羅閱祇耆闍崛山中。與大比丘眾千二百五十人菩薩五千人俱\n" + 
                "。";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("𪘁2", "如1", "是1", "我1", "聞1", "一1", "時1", "佛1", 
                "在1", "羅1", "閱1", "祇1", "耆1", "闍1", "崛1", "山1", "中1", "與1", "大1", "比1", "丘1", "眾1", 
                "千1", "二1", "百1", "五1", "十1", "人1", "菩1", "薩1", "五1", "千1", "人1", "俱1");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        assertCharTokenStream(zhOnly, expected);
    }
    
    @Test
    public void testSimplifiedChinese() throws IOException
    {
        // input and output from https://github.com/BYVoid/OpenCC/tree/master/test/testcases
        String input = "曾經有一份真誠的愛情放在我面前，我沒有珍惜，等我失去的時候我才後悔莫及。"
                + "人事間最痛苦的事莫過於此。如果上天能夠給我一個再來一次得機會，我會對那個女孩子說三個字，"
                + "我愛你。如果非要在這份愛上加個期限，我希望是，一萬年。";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("曾", "经", "有", "一", "份", "真", "诚", 
                "的", "爱", "情", "放", "在", "我", "面", "前", "我", "没", "有", "珍", 
                "惜", "等", "我", "失", "去", "的", "时", "候", "我", "才", "后", "悔", 
                "莫", "及", "人", "事", "间", "最", "痛", "苦", "的", "事", "莫", "过", 
                "于", "此", "如", "果", "上", "天", "能", "够", "给", "我", "一", "个", 
                "再", "来", "一", "次", "得", "机", "会", "我", "会", "对", "那", "个", 
                "女", "孩", "子", "说", "三", "个", "字", "我", "爱", "你", "如", "果", 
                "非", "要", "在", "这", "份", "爱", "上", "加", "个", "期", "限", "我", 
                "希", "望", "是", "一", "万", "年");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        TokenStream sc = new TC2SCFilter(zhOnly);
        assertTokenStream(sc, expected);
    }
    
    @Test
    public void testPinyin() throws IOException
    {
        // https://github.com/axgle/pinyin/blob/master/pinyin_test.go
        String input = "世界 中文 汉字 拼音 简体字 莞 濮 泸 漯 亳 儋";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("shì", "jiè", "zhōng", "wén", "hàn", "zì", 
                "pīn", "yīn", "jiǎn", "tǐ", "zì", "guǎn", "pú", "lú", "luò", "bó", "dān");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        TokenStream pinyin = new ZhToPinyinFilter(zhOnly);
        assertTokenStream(pinyin, expected);
    }
    
    @Test
    public void testTcSc2Pinyin() throws IOException
    {
        // https://github.com/axgle/pinyin/blob/master/pinyin_test.go
        String input = "一一 萬万 年年 經经";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("yī", "yī", "wàn", "wàn", 
                "nián", "nián", "jīng", "jīng");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        TokenStream pinyin = new ZhToPinyinFilter(zhOnly);
        assertTokenStream(pinyin, expected);
    }
    
    @Test
    public void testPY2PY_lazyFilter() throws IOException
    {
        String input = "yī wàn nián jīng āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("yi", "wan", "nian", "jing", 
                "aaaaeeeeiiiioooouuuuuuuu");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream pinyin = new LazyPinyinFilter(words);
        assertTokenStream(pinyin, expected);
    }
    
    @Test
    public void testPinyinTokenizer() throws IOException
    {
        String input = "yiwan nianjing";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("yi", "wan", "nian", "jing");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testStopwordsFilter() throws IOException
    {
        // Google Translate: Not exactly a Chinese word, meaning not exactly.
        String input = "不尽然是一个汉语词汇，意思是不完全如此。";
        Reader reader = new StringReader(input);
        // Google Translate: Chinese vocabulary meaning completely
        List<String> expected = Arrays.asList("汉", "语", "词", "汇", "意", "思", "完", "全");
        System.out.println("0 " + input);
        Reader noStops = new ZhStopWordsFilter(reader);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(noStops, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        assertTokenStream(zhOnly, expected);
    }
    
    @Test
    public void testNumericFilter() throws IOException
    {
        String input = "兆";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("1000000000000");
        System.out.println("0 " + input);
        Reader noStops = new ZhNumericFilter(reader);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(noStops, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testConvertPYstrictWithNumbers() throws IOException
    {
        String input = "yī wàn nián jīng āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("yi", "wan", "nian", "jing", 
                "aaaaeeeeiiiioooouuuuuuuu");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream pinyin = new LazyPinyinFilter(words);
        assertTokenStream(pinyin, expected);
    }
    
    @Test
    public void testConvertPYNumbersToMarks() throws IOException
    {
        // TESTED CASES: 
        // 0: non-pinyin syl, 1: syl without vowel, 
        // 2 and 3: syls with neutral tone
        // 4, 5 and 6: syl with a, e and ou
        // 7: syl where last syl should be marked
        // 8 and 9: syls with ü and v 
        // 10 and later: all normal tones
        String input = "+@/* r3 yi0 yi5 miao1 fei1 zhou3 huo3 lün1 lvn2 yi1 wan4 nian2 jing1 a1 a2 a3 a4 e1 e2 e3 e4 i1 i2 i3 i4 "
                + "o1 o2 o3 o4 u1 u2 u3 u4 v1 v2 v3 v4";
        Reader reader = new StringReader(input); 
        List<String> expected = Arrays.asList("+@/*", "r3", "yi", "yi", "miāo", "fēi", "zhǒu", "huǒ", "lǖn", "lǘn", "yī", "wàn", "nián", 
                "jīng", "ā", "á", "ǎ", "à", "ē", "é", "ě", "è", "ī", "í", "ǐ", "ì", "ō", "ó", "ǒ", "ò", "ū", "ú", 
                "ǔ", "ù", "ǖ", "ǘ", "ǚ", "ǜ");
        System.out.println("0 " + input);
        Tokenizer tok = new WhitespaceTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream pinyin = new PinyinNumberedToMarkedFilter(words);
        assertTokenStream(pinyin, expected);
    }
    
    @Test
    public void testPinyinSyllableTokenizer1() throws IOException
    {
        String input = "changan chang'an biaoier";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("chan", "gan", "chang", "an", "biao", "i", "er");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testSyllabifier1() throws IOException
    {
        // from http://pinyin.info/romanization/hanyu/syllable_boundaries.html
        String input = "fěnbǐ mǎnyì lángān dòngwù bàofēngyǔ bànchéng zhēnshi wǎng zhàn tāngrshì shuǐcōngrshìde";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("fěn", "bǐ", "mǎn", "yì", "lán", "gān", "dòng", "wù", "bào", "fēng", 
                "yǔ", "bàn", "chéng", "zhēn", "shi", "wǎng", "zhàn", "tāng", "r", "shì", "shuǐ", "cōng", "r", "shì", "de");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testSyllabifier2() throws IOException
    {
        // from http://pinyin.info/romanization/hanyu/apostrophes.html
        String input = "cháng'ān chāo'é dān'ǒuhūn tiān'ānmén é'ér dìèr ŌuĀnhuì";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("cháng", "ān", "chāo", "é", "dān", "ǒu", 
                "hūn", "tiān", "ān", "mén", "é", "ér", "dì", "èr", "ōu", "ān", "huì");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testSyllabifier3() throws IOException
    {
        String input = "biaoier";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("biao", "i", "er");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }

    @Test
    public void testSyllabifier4() throws IOException
    {
        // from https://github.com/pepebecker/pinyin-split/blob/master/test/index.js
        String input = "wodemaoxihuanheniunai";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("wo", "de", "mao", "xi", "huan", "he", "niu", "nai");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void testSyllabifier5() throws IOException
    {
        // from https://github.com/pepebecker/pinyin-split/blob/master/test/index.js
        String input = "dérěwǒfèndòudǒuwǒrènwèi";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("dé", "rě", "wǒ", "fèn", "dòu", "dǒu", "wǒ", "rèn", "wèi");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void bugSimplifiedChinese() throws IOException
    {
        // input and output from https://github.com/BYVoid/OpenCC/tree/master/test/testcases
        String input = "祇";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("只");
        System.out.println("0 " + input);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(reader, tok);
        TokenStream zhOnly = new ZhOnlyFilter(words);
        TokenStream sc = new TC2SCFilter(zhOnly);
        assertTokenStream(sc, expected);
    }
    
    @Test
    public void bugSyllabifier1() throws IOException
    {
        String input = "heniu";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("he", "niu");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void bugSyllabifier2() throws IOException
    {
        String input = "rěwǒ";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("rě", "wǒ");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void bugSyllabifier3() throws IOException
    {
        String input = "lángān";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("lán", "gān");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void bugSyllabifier4() throws IOException
    {
        String input = "dòngwù";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("dòng", "wù");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
    
    @Test
    public void bugSyllabifier5() throws IOException
    {
        String input = "tāngrshì";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("tāng", "r", "shì");
        System.out.println("0 " + input);
        Tokenizer tok = new PinyinSyllableTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
}