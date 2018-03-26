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


import java.io.IOException;
import java.io.Reader;
import java.security.InvalidParameterException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * A Chinese Analyzer that uses {@link StandardTokenizer}
 * and {@link ZhOnlyFilter} to only keep Chinese tokens.
 * 
 * The produced tokens are individual ideograms.
 * 
 * @author Hélios Hildt
 **/
public final class ChineseAnalyzer extends Analyzer {
  
    private boolean stopwords = false;
    private String indexEncoding = null;
    private String inputEncoding = null;
    private int variants = -1;    
    
    /**
     * Chinese Analyzer constructor with default values per profile
     * @param profile 
     *              either one of [exactTC, TC, TC2SC, TC2PYstrict, TC2PYlazy, 
     *                             SC, SC2PYstrict, SC2PYlazy,
     *                             PYstrict, PYstrict2PYlazy,
     *                             PYlazy]
     */
    public ChineseAnalyzer(String profile) {
        if (profile.equals("exactTC")) {
            this.inputEncoding = "exact";
            this.indexEncoding = "TC";
        
        } else if (profile.equals("TC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "TC";
        
        } else if (profile.equals("TC2SC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "SC";
        
        } else if (profile.equals("TC2PYmarked")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("TC2PYlazy")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("SC")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "SC";
        
        } else if (profile.equals("SC2PYmarked")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("SC2PYlazy")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYmarked")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("PYmarkedToPYnumbered")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYnumbered";
        
        } else if (profile.equals("PYmarkedToPYlazy")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYnumbered")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYnumbered";
        
        } else if (profile.equals("PYnumberedToPYmarked")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("PYnumberedToPYlazy")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYlazy")) {
            this.inputEncoding = "PYlazy";
            this.indexEncoding = "PYlazy";
        
        } else {
            throw new InvalidParameterException(profile+" is not a supported profile");
        }
        
        if (this.inputEncoding.equals("exact")) {
            this.variants = 0;
            this.stopwords = false;
            this.inputEncoding = "TC";
        
        } else if (this.inputEncoding.startsWith("PY")) {
            this.variants = 0;
            this.stopwords = false;
        
        } else {
            this.variants = 3;
            this.stopwords = true;
        }
    }
    
    /**
     * 
     * @param profile
     *              all profiles except for exactTC
     * @param stopwords
     *              true to filter stopwords, false otherwise
     * @param variants
     *              0: no variant, 1: synonyms, 2: alternatives, 3: both
     */
    public ChineseAnalyzer(String profile, boolean stopwords, int variants) {
        if (profile.equals("TC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "TC";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("TC2SC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "SC";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("TC2PYmarked")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYmarked";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("TC2PYlazy")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYlazy";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("SC")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "SC";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("SC2PYmarked")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYmarked";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("SC2PYlazy")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYlazy";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("PYmarked")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("PYmarkedToPYnumbered")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYnumbered";
        
        } else if (profile.equals("PYmarkedToPYlazy")) {
            this.inputEncoding = "PYmarked";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYnumbered")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYnumbered";
        
        } else if (profile.equals("PYnumberedToPYmarked")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYmarked";
        
        } else if (profile.equals("PYnumberedToPYlazy")) {
            this.inputEncoding = "PYnumbered";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYlazy")) {
            this.inputEncoding = "PYlazy";
            this.indexEncoding = "PYlazy";
        
        } else {
            throw new InvalidParameterException(profile+" is not a supported profile");
        }
    }
    
    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        
        /* if (the input is not PY and we want to filter stopwords) */
        if (!this.inputEncoding.startsWith("PY") && this.stopwords) {
            try {
                reader = new ZhStopWordsFilter(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
         /* normalize all incoming Pinyin */
        if (this.inputEncoding.startsWith("PY")) {
            reader = new PinyinNormalizingFilter(reader);
        }
        
        return super.initReader(fieldName, reader);
    }
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {        
        /* tokenizes in ideograms or in words separated by punctuation.*/
        Tokenizer tok = null;
        TokenStream tokenStream = null;
        
        if (this.inputEncoding.startsWith("PY")) {
            try {
                tok = new PinyinSyllableTokenizer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            tok = new StandardTokenizer();
        }
        
        /* if (input is either TC or SC) */
        if (this.inputEncoding.endsWith("C")) {
            /* only keep TC tokens */
            tokenStream = new ZhOnlyFilter(tok);
            
            /* apply variant filters */
            if (variants == 0) {
                // pass
            } else if (variants == 1) {
                try {
                    tokenStream = new ZhSynonymFilter(tokenStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            } else if (variants == 2) {
                try {
                    tokenStream = new ZhAlternatesFilter(tokenStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            } else if (variants == 3) {
                try {
                    tokenStream = new ZhSynonymFilter(tokenStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    tokenStream = new ZhAlternatesFilter(tokenStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* indexing from TC to SC */
        if (this.indexEncoding.equals("SC") && this.inputEncoding.equals("TC")) {
            try {
                tokenStream = new TC2SCFilter(tokenStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        /* indexing from ideograms to pinyin */
        } else if (this.indexEncoding.startsWith("PY") && this.inputEncoding.endsWith("C")) {
            try {
                tokenStream = new ZhToPinyinFilter(tokenStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (this.indexEncoding.equals("PYlazy")) {
                tokenStream = new LazyPinyinFilter(tokenStream);
            }
        }
        
        /* indexing from any encoding to PYlazy */
        if (this.inputEncoding.startsWith("PY")) {            
            if (this.indexEncoding.equals("PYlazy") && !this.inputEncoding.equals("PYlazy")) {
                if (this.inputEncoding.equals("PYnumbered")) {
                    try {
                        tokenStream = new PinyinNumberedToMarkedFilter(tok);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tokenStream = new LazyPinyinFilter(tokenStream);
                } else {
                    tokenStream = new LazyPinyinFilter(tok);
                }
            } else {
                return new TokenStreamComponents(tok);
            }
        }
        return new TokenStreamComponents(tok, tokenStream);
    }
}
