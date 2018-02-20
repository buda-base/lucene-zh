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
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

/**
 * Unit tests for the Sanskrit tokenizers and filters.
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
                termList.add(charTermAttribute.toString());
            }
            System.out.println("1 " + String.join(" ", expected));
            System.out.println("2 " + String.join(" ", termList) + "\n");
            for (String term: termList) {
                System.out.println(term +": " + term.getBytes().length + " bytes, but length of string is:" + term.length());
            }
            assertThat(termList, is(expected));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
    
    @Test
    public void test2() throws IOException
    {
        String input = "𪘁  \u2A601  如抜範浪偃壅國  二親歸依三寶。時從大德調!伏軍教誦四阿";
        Reader reader = new StringReader(input);
        List<String> expected = Arrays.asList("二", "親", "歸", "依", "三", "寶", "時", "從",
                "大", "德", "調", "伏", "軍", "教", "誦", "四", "阿");
        System.out.println("0 " + input);
        Tokenizer tok = new ClassicTokenizer();
        TokenStream words = tokenize(reader, tok);
        assertTokenStream(words, expected);
    }
}