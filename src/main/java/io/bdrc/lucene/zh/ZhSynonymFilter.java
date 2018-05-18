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
import java.util.HashMap;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Maps all synonyms to a single variant.
 * 
 * The first synonym occuring in Unihan is arbitrarily chosen.
 * 
 * uses data from
 * @see <a href="https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data">lucene-zh-data</a>
 * 
 * @author Hélios Hildt
 *
 */

public class ZhSynonymFilter extends TokenFilter {

    private static final HashMap<String, String> map = CommonHelpers.getMappings("zh-synonyms.tsv");
    CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

    public ZhSynonymFilter(TokenStream in) {
        super(in);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            final String sc = map.get(charTermAttribute.toString());
            if (sc != null) {
                charTermAttribute.setEmpty().append(sc);
            }
            return true;
        }
        return false;
    }
}
