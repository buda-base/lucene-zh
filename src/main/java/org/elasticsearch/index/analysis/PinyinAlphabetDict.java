package org.elasticsearch.index.analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


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