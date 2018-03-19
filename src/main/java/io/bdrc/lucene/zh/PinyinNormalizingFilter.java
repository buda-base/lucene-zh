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

import java.io.Reader;

import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

/**
 * Pinyin Normalizer
 * 
 * This Filter: 
 *          - normalizes breves existing in the wild into carons to conform with the Unihan convention.
 *          - deletes accents (acute, grave and carons) when found on "m" and "n" (found in Unihan data)
 *          - combines grave accents to the preceding vowel (lone grave accent found in Unihan data)
 *          - deletes grave accents not preceded by a, i, o, u and ü
 *          - keep the case of the characters. PinyinSyllabifyingFilter takes care of lowercasing.
 *           
 * uses data from
 * {@link https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data}
 * 
 * @author Hélios Hildt
 *
 */
public class PinyinNormalizingFilter extends MappingCharFilter {

    public PinyinNormalizingFilter(Reader in) {
        super(getNormalizeCharMap(), in);
    }

    public final static NormalizeCharMap getNormalizeCharMap() {
        final NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();

        // breve diacritics are normalized to caron
        builder.add("\u0102", "Ǎ");
        builder.add("\u0103", "ǎ");
        builder.add("\u0114", "Ě");
        builder.add("\u0115", "ě");
        builder.add("\u012C", "Ǐ");
        builder.add("\u012D", "ǐ");
        builder.add("\u014E", "Ǒ");
        builder.add("\u014F", "ǒ");
        builder.add("\u016C", "Ǔ");
        builder.add("\u016D", "ǔ");
        // ü + breve does not exist

        // unexpected because not vowels
        builder.add("Ḿ", "M");
        builder.add("ḿ", "m");
        builder.add("Ń", "N");
        builder.add("ń", "n");
        builder.add("Ǹ", "N");
        builder.add("ǹ", "n");
        builder.add("Ň", "N");
        builder.add("ň", "n");

        // U+0300 Combining Grave Accent  
        builder.add("a\u0300", "à");
        builder.add("e\u0300", "è");
        builder.add("i\u0300", "ì");
        builder.add("o\u0300", "ò");
        builder.add("u\u0300", "ù");
        builder.add("ü\u0300", "ǜ");
        builder.add("̀", "");    // deleting all other occurences

        return builder.build();
    }
}
