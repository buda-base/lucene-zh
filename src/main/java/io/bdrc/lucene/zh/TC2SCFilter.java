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
import java.util.HashMap;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Traditional Chinese to Simplified Chinese charfilter
 * 
 * uses data from
 * @see <a href="https://github.com/BuddhistDigitalResourceCenter/lucene-zh-data">lucene-zh-data</a>
 * 
 * @author Hélios Hildt
 *
 */

public class TC2SCFilter extends TokenFilter {

    private HashMap<String, String> map;

    public TC2SCFilter(TokenStream in) throws IOException {
        super(in);
        map = getMapping();
    }

    public final HashMap<String, String> getMapping() throws IOException {
        String fileName = "src/main/resources/tc2sc.tsv";
        BufferedReader br;
        InputStream stream = null;
        stream = TC2SCFilter.class.getResourceAsStream("/tc2sc.tsv");
        if (stream == null) { // we're not using the jar, these is no resource, assuming we're running the
                              // code
            br = new BufferedReader(new FileReader(fileName));
        } else {
            br = new BufferedReader(new InputStreamReader(stream));
        }

        final HashMap<String, String> map = new HashMap<String, String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            map.put(parts[0], parts[1]);
        }
        br.close();
        return map;
    }

    CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

    @Override
    public final boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String sc = map.get(charTermAttribute.toString());
            if (sc != null) {
                charTermAttribute.setEmpty().append(sc);
            }
            return true;
        }
        return false;
    }
}
