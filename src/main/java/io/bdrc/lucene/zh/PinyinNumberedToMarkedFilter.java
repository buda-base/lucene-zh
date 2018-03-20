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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Token Filter to convert a Pinyin syllable with tone numbers to tone marks
 * 
 *  Implements the following algorithm(from {@link https://github.com/tsroten/zhon/blob/develop/tests/test-pinyin.py#L99}:
 *
 *     1. If the syllable has an 'a' or 'e', put the tone over that vowel.
 *     2. If the syllable has 'ou', place the tone over the 'o'.
 *     3. Otherwise, put the tone on the last vowel.
 *  
 *  If the syllable does not end with a tone number (from 0 to 5), the syllable is returned as is.
 * 
 * @author Hélios Hildt
 *
 */
public class PinyinNumberedToMarkedFilter extends TokenFilter {

    public PinyinNumberedToMarkedFilter(TokenStream in) throws IOException {
        super(in);
    }

    /**
     * Converts a Pinyin syllable with tone numbers to tone marks
     *  
     * @param pinyinStr syllable to process 
     * @return
     */
    public String numberedToMarked(String pinyinStr) {
        List<Character> toneNumbers = Arrays.asList('1', '2', '3', '4', '5', '0');
        final String markedVowels = "āáǎàaēéěèeīíǐìiōóǒòoūúǔùuǖǘǚǜü";
        HashMap<Character, Integer> rows = new HashMap<Character, Integer>();
        rows.put('a', 0);
        rows.put('e', 1);
        rows.put('i', 2);
        rows.put('o', 3);
        rows.put('u', 4);
        rows.put('v', 5);
        
        char number = pinyinStr.charAt(pinyinStr.length()-1);
        if (toneNumbers.contains(number)) {
            /* find the index of the vowel to mark */
            int toMarkIdx = -1;
            char toMarkVowel;
            
            int aIdx = pinyinStr.indexOf('a');
            int eIdx = pinyinStr.indexOf('e');
            int oIdx = pinyinStr.indexOf("ou");
            
            if (aIdx != -1) {
                toMarkIdx = aIdx;
                toMarkVowel = 'a';
            } else if (eIdx != -1) {
                toMarkIdx = eIdx;
                toMarkVowel = 'e';
            } else if (oIdx != -1) {
                toMarkIdx = oIdx;
                toMarkVowel = 'o';
            } else {
                List<Integer> otherVowels = Arrays.asList(
                        pinyinStr.lastIndexOf('i'),
                        pinyinStr.lastIndexOf('o'),
                        pinyinStr.lastIndexOf('u'),
                        pinyinStr.lastIndexOf('v'));
                Collections.sort(otherVowels);
                Collections.reverse(otherVowels);
                toMarkIdx = otherVowels.get(0);
                toMarkVowel = pinyinStr.charAt(toMarkIdx);
            }
            
            /* if (there is a vowel to mark) */
            if (toMarkIdx != -1) {
                char newVowel = markedVowels.charAt(rows.get(toMarkVowel) * 5 
                        + Character.getNumericValue(number) - 1);
                
                StringBuffer marked = new StringBuffer(pinyinStr);
                marked.setLength(pinyinStr.length() - 1);   // remove tone number
                marked.deleteCharAt(toMarkIdx);             // delete vowel without tone
                marked.insert(toMarkIdx, newVowel);         // insert marked vowel
                return marked.toString();
            } else {
                return pinyinStr;
            }
        } else {
            return pinyinStr;
        }
    }
    
    CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

    @Override
    public final boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String marked = numberedToMarked(charTermAttribute.toString());
            charTermAttribute.setEmpty().append(marked);
            return true;
        }
        return false;
    }
}