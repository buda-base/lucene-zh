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
 * Maps all numeric ideograms to the corresponding numbers.
 * 
 * For ex: "一", "壱", "壹" and "弌"  all map to "1"
 * 
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
        String fileName = "src/main/resources/zh-numbers.txt";
        BufferedReader br;
        InputStream stream = null;
        stream = ZhNumericFilter.class.getResourceAsStream("/zh-numbers.txt");
        if (stream == null ) {    // we're not using the jar, these is no resource, assuming we're running the code
             br = new BufferedReader(new FileReader(fileName));
        } else {
            br = new BufferedReader(new InputStreamReader(stream));
        }

        final NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        String line = null;
        while ((line = br.readLine()) != null) {
            builder.add(line, "");  // we want to map all stopword sequences to nothing to delete them
        }
        br.close();

        return builder.build();
    }
}