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
 * !!! IMPORTANT
 * As the data from zh-numbers.tsv is not comprehensive enough to correctly parse all numerals 
 * to their arabic counterparts, this Filter is not used in ChineseAnalyzer 
 * (See @see <a href="http://www.mandarintools.com/numbers.html">here</a>)
 * 
 * The article at "https://en.wikibooks.org/wiki/Chinese_(Mandarin)/Numbers" also shows that these numeral
 * ideograms have different meanings, like: '貳(2) can also mean "to betray"'. 
 * !!!
 * 
 * Maps all numeric ideograms to the corresponding numbers.
 * 
 * For ex: "一", "壱", "壹" and "弌"  all map to "1"
 * 
 * Note: ZhOnlyFilter deletes the arabic numbers coming out of ZhNumericFilter.
 * @see <a href="https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data">lucene-zh-data</a>
 * 
 * @author Hélios Hildt
 *
 */

public class ZhNumericFilter extends MappingCharFilter {
    
    private static final NormalizeCharMap map = CommonHelpers.getNormalizeCharMap("zh-numbers.tsv", false);
    
    public ZhNumericFilter(Reader in) {
        super(map, in);
    }
}