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
import org.apache.lucene.analysis.core.LowerCaseFilter;
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
            this.inputEncoding = "TC";
            this.indexEncoding = "TC";
        
        } else if (profile.equals("TC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "TC";
        
        } else if (profile.equals("TC2SC")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "SC";
        
        } else if (profile.equals("TC2PYstrict")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYstrict";
        
        } else if (profile.equals("TC2PYlazy")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("SC")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "SC";
        
        } else if (profile.equals("SC2PYstrict")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYstrict";
        
        } else if (profile.equals("SC2PYlazy")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYstrict")) {
            this.inputEncoding = "PYstrict";
            this.indexEncoding = "PYstrict";
        
        } else if (profile.equals("PYstrict2PYlazy")) {
            this.inputEncoding = "PYstrict";
            this.indexEncoding = "PYlazy";
        
        } else if (profile.equals("PYlazy")) {
            this.inputEncoding = "PYlazy";
            this.indexEncoding = "PYlazy";
            
        } else {
            throw new InvalidParameterException(profile+" is not a supported profile");
        }
        
        if (this.inputEncoding.startsWith("exact")) {
            this.variants = 0;
            this.stopwords = false;
        
        } else if (this.inputEncoding.startsWith("PY")) {
            this.variants = 1;  // because alternatives are stylistic variants of ideograms
            this.stopwords = true;
        
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
        
        } else if (profile.equals("TC2PYstrict")) {
            this.inputEncoding = "TC";
            this.indexEncoding = "PYstrict";
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
        
        } else if (profile.equals("SC2PYstrict")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYstrict";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("SC2PYlazy")) {
            this.inputEncoding = "SC";
            this.indexEncoding = "PYlazy";
            this.variants = variants;
            this.stopwords = stopwords;
        
        } else if (profile.equals("PYstrict")) {
            this.inputEncoding = "PYstrict";
            this.indexEncoding = "PYstrict";
            this.stopwords = true;
        
        } else if (profile.equals("PYstrict2PYlazy")) {
            this.inputEncoding = "PYstrict";
            this.indexEncoding = "PYlazy";
            this.stopwords = true;
        
        } else if (profile.equals("PYlazy")) {
            this.inputEncoding = "PYlazy";
            this.indexEncoding = "PYlazy";
            this.stopwords = true;
        
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
            return super.initReader(fieldName, reader);
        
        } else {
            return super.initReader(fieldName, reader);
        }
    }
    
    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {        
        /* tokenizes in ideograms or in words separated by punctuation.*/
        TokenStream tokenStream = new StandardTokenizer();
        
        if (this.inputEncoding.equals("TC")) {
            /* only keep TC tokens */
            tokenStream = new ZhOnlyFilter(tokenStream);
            
            if (this.indexEncoding.equals("TC")) { 
                if (variants == 0) {
                    
                } else if (variants == 1) {
                    
                } else if (variants == 2) {
                    
                } else if (variants == 3) {
                    
                }
            } else if (this.indexEncoding.equals("SC")) {
                
            } else if (this.indexEncoding.equals("PYstrict")) {
                
            } else if (this.indexEncoding.equals("PYlazy")) {
                
            }
            
            if (!this.indexEncoding.equals("exact")) {
                if (true) {
                    
                }
            }
        }

        if (this.inputEncoding.equals("SC")) {
            /* only keep TC tokens */
            tokenStream = new ZhOnlyFilter(tokenStream);
        }

        if (this.indexEncoding.startsWith("PY")) {
            /* Syllabify Pinyin */
            // syllabify method
            
            if (this.inputEncoding.equals("PYstrict")) {
                tokenStream = new LowerCaseFilter(tokenStream);
            }

            if (this.inputEncoding.equals("PYlazy")) {
                tokenStream = new LowerCaseFilter(tokenStream);
            }
        }
        
        return new TokenStreamComponents((Tokenizer) tokenStream);
    }
}
