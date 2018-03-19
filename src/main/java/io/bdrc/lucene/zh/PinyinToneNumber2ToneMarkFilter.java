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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.nlpcn.commons.lang.pinyin.PinyinFormatter;
import org.nlpcn.commons.lang.pinyin.PinyinFormat;

/**
 * Traditional Chinese -> Simplified Chinese charfilter
 * 
 * 
 * @author Hélios Hildt
 *
 */

public class PinyinToneNumber2ToneMarkFilter extends TokenFilter {

    public PinyinToneNumber2ToneMarkFilter(TokenStream in) throws IOException {
        super(in);
    }

    CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

    @Override
    public final boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String marked = PinyinFormatter.formatPinyin(charTermAttribute.toString(), PinyinFormat.UNICODE_PINYIN_FORMAT);
            // POST-PROCESSING
            // chars with breve are replaced by caron (as in Unihan)
            marked = marked.replaceAll("ă", "ǎ");
            marked = marked.replaceAll("ĕ", "ě");
            marked = marked.replaceAll("ĭ", "ǐ");
            marked = marked.replaceAll("ŏ", "ǒ");
            marked = marked.replaceAll("ŭ", "ǔ");
            charTermAttribute.setEmpty().append(marked);
            return true;
        }
        return false;
    }
}