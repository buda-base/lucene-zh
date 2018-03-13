package io.bdrc.lucene.zh.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import io.bdrc.lucene.zh.ChineseAnalyzer;

public class PrettyPrintResult {
    
    static OutputStreamWriter writer;
    
    /**
     * 
     * @param args
     *              args[0]: the file to be processed, preferably one word per line
     *              remaining ones: profiles to be used
     * Output:
     *          for a file containing "一\n一些\n一何" and "TC2SC TC2PYstrict TC2PYlazy"
     *          the output is:
     *          
     *          orig  TC2SC  TC2PYstrict TC2PYlazy   
     *          一     一     yī          yi  
     *          一些   一些    yīxiē       yixie   
     *          一何   一何    yīhé        yihe    
     */
    public static void main(String[] args) throws FileNotFoundException, IOException{
                
        String file = args[0];
        
        String[] profiles = Arrays.copyOfRange(args, 1, args.length);
        
        String outFileName = file.substring(0, file.lastIndexOf('.')) + "_analyzed.txt";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        writer = new OutputStreamWriter(new FileOutputStream(outFileName), StandardCharsets.UTF_8);
        System.out.println("Processing " + file + "...");
        writer.write("orig\t");
        for (String p: profiles) {
            writer.write(p + "\t");
        }
        writer.write("\n");
        
        String line = null;
        while ((line = reader.readLine()) != null) {
            // ignore any BOM marker on first line
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            String processedLine = line + "\t";
            for (String profile: profiles) {
                List<String> parsed = parseTokens(new ChineseAnalyzer(profile, false, 0), line);
                if (parsed.isEmpty()) {
                    processedLine += "\t";
                }
                String joined = String.join("-", parsed);
                processedLine += joined + "\t";
            }
            writer.write(processedLine + "\n");
            writer.flush();
        }
        reader.close();
        writer.close();
    }
    
    // adapted from https://stackoverflow.com/a/9562816
    public static List<String> parseTokens(Analyzer analyzer, String input) throws IOException {

        List<String> result = new ArrayList<String>();
        TokenStream stream  = analyzer.tokenStream(null, new StringReader(input));
        stream.reset();
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        
        try {
            while(stream.incrementToken()) {
                result.add(charTermAttribute.toString());
            }
        }
        catch(IOException e) {
            // not thrown b/c we're using a string reader...
        }
        stream.close();
        return result;
    }  
}
