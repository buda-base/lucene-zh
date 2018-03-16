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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * Taken from {@link https://github.com/medcl/elasticsearch-analysis-pinyin}
 * and adapted to fit in PinyinTokenFilter.
 * 
 * Created by medcl ({@link https://github.com/medcl}) on 16/10/13.
 * 
 */
public class PinyinAlphabetDict {

    private Set<String> alphabet = new HashSet<String>();
    
    private static PinyinAlphabetDict instance;
    
    public PinyinAlphabetDict() {
        String fileName = "src/main/resources/pinyin_alphabet_expanded.txt";
        BufferedReader reader = null;
        InputStream stream = null;
        stream = PinyinAlphabetDict.class.getResourceAsStream("/pinyin_alphabet_expanded.txt");
        if (stream == null) { // we're not using the jar, these is no resource, assuming we're running the
                              // code
            try {
                reader = new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            reader = new BufferedReader(new InputStreamReader(stream));
        }

        try {
            String line;
            while (null != (line = reader.readLine())) {
                if (line.trim().length() > 0) {
                    alphabet.add(line);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("read pinyin dic error.", ex);
        } finally {
            try {
                reader.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static PinyinAlphabetDict getInstance() {
        if (instance == null) {
            synchronized (PinyinAlphabetDict.class) {
                if (instance == null) {
                    instance = new PinyinAlphabetDict();
                }
            }
        }
        return instance;
    }

    public boolean match(String c) {
        return alphabet.contains(c);
    }
}