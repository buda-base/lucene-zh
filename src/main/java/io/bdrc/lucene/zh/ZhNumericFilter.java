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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

/**
 * !!! IMPORTANT
 * As the data from zh-numbers.tsv is not comprehensive enough to correctly parse all numerals 
 * to their arabic counterparts, this Filter is not used in ChineseAnalyzer 
 * (See {@link http://www.mandarintools.com/numbers.html})
 * 
 * {@link https://en.wikibooks.org/wiki/Chinese_(Mandarin)/Numbers} also shows that these numeral
 * ideograms have different meanings, like: '貳(2) can also mean "to betray"'. 
 * !!!
 * 
 * Maps all numeric ideograms to the corresponding numbers.
 * 
 * For ex: "一", "壱", "壹" and "弌"  all map to "1"
 * 
 * Note: ZhOnlyFilter deletes the arabic numbers coming out of ZhNumericFilter.
 * uses data from {@link https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data}
 * 
 * @author Hélios Hildt
 *
 */

public class ZhNumericFilter extends MappingCharFilter {

    public ZhNumericFilter(Reader in) throws IOException {
        super(getCnNormalizeCharMap(), in);
    }

    public final static NormalizeCharMap getCnNormalizeCharMap() throws IOException {
        String fileName = "src/main/resources/zh-numbers.tsv";
        BufferedReader br;
        InputStream stream = null;
        stream = ZhNumericFilter.class.getResourceAsStream("/zh-numbers.tsv");
        if (stream == null ) {    // we're not using the jar, these is no resource, assuming we're running the code
             br = new BufferedReader(new FileReader(fileName));
        } else {
            br = new BufferedReader(new InputStreamReader(stream));
        }

        final NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] parts= line.split("\t");
            builder.add(parts[0], parts[1]);
        }
        br.close();

        return builder.build();
    }
}