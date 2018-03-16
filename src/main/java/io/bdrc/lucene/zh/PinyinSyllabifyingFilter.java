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
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * A Pinyin syllable Filter that uses PinyinAlphabetTokenizer under the hood
 * 
 * note: see the limitation described in PinyinAlphabetTokenizer
 * 
 * @author drupchen
 *
 */
public class PinyinSyllabifyingFilter extends TokenFilter{
    LinkedList<String> tokens = new LinkedList<String>();
    int start = 0;
    int end = 0;
    
    protected PinyinSyllabifyingFilter(TokenStream in) {
        super(in);
        
        // TODO Auto-generated constructor stub
    }
    
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    
    @Override
    public final boolean incrementToken() throws IOException {
        /* initialize the tokens */
        if (tokens.isEmpty()) {
            input.incrementToken();
            start = offsetAtt.startOffset();
            tokens.addAll(PinyinAlphabetTokenizer.walk(termAtt.toString()));
        }
        
        while (!tokens.isEmpty()) {
            String token = tokens.removeFirst();
            
            /* update offsets */
            start = end;
            end += token.length();
            
            if (PinyinAlphabetDict.getInstance().match(token)) {
                termAtt.setEmpty().append(token);
                
                termAtt.setLength(end - start);
                offsetAtt.setOffset(start, end);
                
                return true;
            } else {
                /* initialize the tokens */
                if (tokens.isEmpty()) {
                    input.incrementToken();
                    start = offsetAtt.startOffset();
                    tokens.addAll(PinyinAlphabetTokenizer.walk(termAtt.toString()));
                }
                continue;
            }
        }
        return false;        
    }
}
