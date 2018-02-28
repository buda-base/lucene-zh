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


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.IOUtils;

/**
 * A Chinese Analyzer that uses {@link StandardTokenizer}
 * and {@link ZhOnlyFilter} to only keep Chinese tokens.
 * 
 * The produced tokens are individual ideograms.
 * 
 * @author Hélios Hildt
 **/
public final class ChineseAnalyzer extends Analyzer {
  
    private int encoding;
    CharArraySet zhStopWords;

    /**
     * Creates a new {@link ChineseAnalyzer} with the default values
     * @throws IOException  stopwords
     * @throws FileNotFoundException 
     */
    public ChineseAnalyzer() throws FileNotFoundException, IOException {
        this(0, "resources/stopwords-zy.txt");
    }
  
    public ChineseAnalyzer(int encoding, String stopFilename) throws FileNotFoundException, IOException {
        this.encoding = encoding;
        if (stopFilename != null) {
            InputStream stream = null;
            stream = ChineseAnalyzer.class.getResourceAsStream("/stopwords-zh.txt");
            if (stream == null ) {    // we're not using the jar, these is no resource, assuming we're running the code
                this.zhStopWords = StopFilter.makeStopSet(getWordList(new FileInputStream(stopFilename), "#"));
            } else {
                this.zhStopWords = StopFilter.makeStopSet(getWordList(stream, "#"));
            }
        } else {
            this.zhStopWords = null;
        }
    }
  
    /**
     * @param reader Reader containing the list of stopwords
     * @param comment The string representing a comment.
     * @return result the {@link ArrayList} to fill with the reader's words
     */
    public static ArrayList<String> getWordList(InputStream inputStream, String comment) throws IOException {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String word = null;
            while ((word = br.readLine()) != null) {
                word = word.replace("\t", "");
                if (word.contains(comment)) {
                    if (!word.startsWith(comment)) {
                        word = word.substring(0, word.indexOf(comment));
                        word = word.trim();
                        if (!word.isEmpty()) result.add(word);
                    }
                } else {
                    word = word.trim();
                    if (!word.isEmpty()) result.add(word);
                }
            }
        }
        finally {
            IOUtils.close(br);
        }
        return result;
    }
    
    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        
        /* if (we want to index in SC or in pinyin) */
        if (this.encoding == 1 || this.encoding == 2) {
            try {
                reader = new ZhStopWordsFilter(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.initReader(fieldName, reader);
        
        } else {
            return super.initReader(fieldName, reader);
        }
    }
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {        
        TokenStream tokenStream = new StandardTokenizer();
        tokenStream = new ZhOnlyFilter(tokenStream);
        
        /* if (we want to index in SC) */
        if (this.encoding == 1) {
            try {
                tokenStream = new TC2SCFilter(tokenStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        /* if (we want to index in pinyin) */
        } else if (this.encoding == 2) {
            try {
                tokenStream = new PinyinFilter(tokenStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new TokenStreamComponents((Tokenizer) tokenStream);
    }
}
