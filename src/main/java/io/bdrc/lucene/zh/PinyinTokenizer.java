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
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * A Pinyin syllable Tokenizer that uses PinyinAlphabetTokenizer under the hood
 * 
 * note: see the limitation described in PinyinAlphabetTokenizer
 * 
 * @author drupchen
 *
 */
public class PinyinTokenizer extends Tokenizer{
    List<String> tokens = null;
    int idx = 0;
    int start = 0;
    int end = 0;
    
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    
    StringBuilder builder = null;
    
    @Override
    public final boolean incrementToken() throws IOException {
        /* getting the string back from the input Reader */
        if (builder == null) {
            // adapted from https://stackoverflow.com/a/17751100
            builder = new StringBuilder();
            int charsRead = -1;
            char[] chars = new char[100];
            do {
                charsRead = input.read(chars,0,chars.length);
                //if we have valid chars, append them to end of string.
                if(charsRead>0)
                    builder.append(chars,0,charsRead);
            } while(charsRead>0);
        }
        
        
        /* initialize the tokens */
        if (tokens == null) {
            tokens = PinyinAlphabetTokenizer.walk(builder.toString());
        }
        
        
        
        while (idx < tokens.size()) {
            String token = tokens.get(idx);
            
            /* update offsets */
            start = end;
            end += token.length();
            
            if (PinyinAlphabetDict.getInstance().match(token)) {
                /*  */
                termAtt.setEmpty().append(token);
                
                termAtt.setLength(end - start);
                offsetAtt.setOffset(correctOffset(start), correctOffset(end));
                
                idx ++;
                return true;
            } else {
                idx ++;
                continue;
            }
        }
        return false;
    }
}
