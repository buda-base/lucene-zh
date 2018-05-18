package io.bdrc.lucene.zh;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonHelpers {
    private static final Logger logger = LoggerFactory.getLogger(CommonHelpers.class);
    public static final String baseDir = "src/main/resources/";
    
    public static InputStream getResourceOrFile(final String baseName) {
        InputStream stream = null;
        stream = CommonHelpers.class.getClassLoader().getResourceAsStream("/"+baseName);
        if (stream != null) {
            logger.info("found resource /{} through regular classloader", baseName);
            return stream;
        }
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/"+baseName);
        if (stream != null) {
            logger.info("found resource /{} through thread context classloader", baseName);
            return stream;
        }
        final String fileBaseName = baseDir+baseName;
        try {
            stream = new FileInputStream(fileBaseName);
            logger.info("found file {}", fileBaseName);
            return stream;
        } catch (FileNotFoundException e) {
            logger.info("could not find file {}", fileBaseName);
            return null;
        }  
    }
    
    public static final HashMap<String, String> getMappings (final String baseName) {
        final InputStream stream = CommonHelpers.getResourceOrFile(baseName);
        final HashMap<String, String> map = new HashMap<String, String>();
        if (stream == null) {
            logger.error("cannot find {}, no mapping will occur", baseName);
            return null;
        }
        final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                final String[] parts = line.split("\t");
                map.put(parts[0], parts[1]);
            }
            br.close();
        } catch (IOException e) {
            logger.error("problem when reading "+baseName, e);
            return map;
        }
        return map;
    }

    public final static NormalizeCharMap getNormalizeCharMap(final String baseName, boolean oneColumn) {
        final InputStream stream = CommonHelpers.getResourceOrFile(baseName);
        final NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
        if (stream == null) {
            logger.error("cannot find {}, no mapping will occur", baseName);
            return builder.build();
        }
        final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                if (oneColumn) {
                    builder.add(line, "");
                } else {
                    final String[] parts= line.split("\t");
                    builder.add(parts[0], parts[1]);
                }
            }
            br.close();
        } catch (IOException e) {
            logger.error("problem when reading "+baseName, e);
            return builder.build();
        }
        return builder.build();
    }
}
