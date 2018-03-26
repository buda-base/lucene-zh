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
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Pinyin to Lazy Pinyin charfilter
 * 
 * uses data from
 * @see <a href="https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data">lucene-zh-data</a>
 * 
 * @author Hélios Hildt
 *
 */

public class LazyPinyinFilter extends TokenFilter {

    private HashMap<String, String> map;
    private List<Character> pinyinNumbers = Arrays.asList('0', '1', '2', '3', '4', '5');

    public LazyPinyinFilter(TokenStream in) {
        super(in);
        map = getMapping();
    }

    public final HashMap<String, String> getMapping() {
        map = new HashMap<String, String>();
        
        map.put("ā", "a");
        map.put("á", "a");
        map.put("ǎ", "a");
        map.put("à", "a");
        map.put("ē", "e");
        map.put("é", "e");
        map.put("ě", "e");
        map.put("è", "e");
        map.put("ī", "i");
        map.put("í", "i");
        map.put("ǐ", "i");
        map.put("ì", "i");
        map.put("ō", "o");
        map.put("ó", "o");
        map.put("ǒ", "o");
        map.put("ò", "o");
        map.put("ū", "u");
        map.put("ú", "u");
        map.put("ǔ", "u");
        map.put("ù", "u");
        map.put("ǖ", "u");
        map.put("ǘ", "u");
        map.put("ǚ", "u");
        map.put("ǜ", "u");
        // unexpected because not vowels, yet found in Unihan Pinyin fields
        map.put("̀", "");    // U+0300 Combining Grave Accent (simply deleted here)
        map.put("ḿ", "m");
        map.put("ǹ", "n");
        map.put("ń", "n");
        map.put("ň", "n");
        return map;
    }

    CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    @Override
    public final boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            StringBuilder lazied = new StringBuilder();
            char[] tokenBuffer = charTermAttribute.toString().toCharArray();
            for (char t: tokenBuffer) {
                String key = String.valueOf(t);
                if (map.containsKey(key)) {
                    lazied.append(map.get(key));
                } else {
                    lazied.append(t);
                }
            }
            if (typeAttribute.type().equals("word") && pinyinNumbers.contains(lazied.charAt(lazied.length() - 1))) {
                lazied.setLength(lazied.length() - 1);
            }
            charTermAttribute.setEmpty().append(lazied.toString());
            return true;
        }
        return false;
    }
}