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
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

/**
 * Unit tests for the Chinese tokenizers and filters.
 * 
 * @author Hélios Hildt
 */
public class ChineseAnalyzerTests  {

    static TokenStream tokenize(Reader reader, Tokenizer tokenizer) throws IOException {
        tokenizer.close();
        tokenizer.end();
        tokenizer.setReader(reader);
        tokenizer.reset();
        return tokenizer;
    }
    
    static private void assertTokenStream(TokenStream tokenStream, List<String> expected) {
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
    
    @Test
    public void test1() throws IOException
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
        TokenStream cnOnly = new CnOnlyFilter(words);
        assertTokenStream(cnOnly, expected);
    }
    
    @Test
    public void testSimplifiedChinese() throws IOException
    {
        // input and output from https://github.com/BYVoid/OpenCC/tree/master/test/testcases
        String input = "曾經有一份真誠的愛情放在我面前，我沒有珍惜，等我失去的時候我才後悔莫及。"
                + "人事間最痛苦的事莫過於此。如果上天能夠給我一個再來一次得機會，我會對那個女孩子說三個字，"
                + "我愛你。如果非要在這份愛上加個期限，我希望是，一萬年。";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("曾1", "经1", "有1", "一1", "份1", "真1", "诚1", 
                "的1", "爱1", "情1", "放1", "在1", "我1", "面1", "前1", "我1", "没1", "有1", "珍1", 
                "惜1", "等1", "我1", "失1", "去1", "的1", "时1", "候1", "我1", "才1", "后1", "悔1", 
                "莫1", "及1", "人1", "事1", "间1", "最1", "痛1", "苦1", "的1", "事1", "莫1", "过1", 
                "于1", "此1", "如1", "果1", "上1", "天1", "能1", "够1", "给1", "我1", "一1", "个1", 
                "再1", "来1", "一1", "次1", "得1", "机1", "会1", "我1", "会1", "对1", "那1", "个1", 
                "女1", "孩1", "子1", "说1", "三1", "个1", "字1", "我1", "爱1", "你1", "如1", "果1", 
                "非1", "要1", "在1", "这1", "份1", "爱1", "上1", "加1", "个1", "期1", "限1", "我1", 
                "希1", "望1", "是1", "一1", "万1", "年1");
        System.out.println("0 " + input);
        Reader sc = new TC2SCFilter(reader);
        Tokenizer tok = new StandardTokenizer();
        TokenStream words = tokenize(sc, tok);
        TokenStream cnOnly = new CnOnlyFilter(words);
        assertTokenStream(cnOnly, expected);
    }
}