package io.bdrc.lucene.zh;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.junit.Test;

public class HighlighterTest {
    static TokenStream tokenize(Reader reader, Tokenizer tokenizer) throws IOException {
        tokenizer.close();
        tokenizer.end();
        tokenizer.setReader(reader);
        tokenizer.reset();
        return tokenizer;
    }
    
    static private void assertTokenStream(TokenStream tokenStream, List<String> expected) {
        try {
            final List<String> termList = new ArrayList<String>();
            final CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            final OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
            while (tokenStream.incrementToken()) {
                termList.add(charTermAttribute.toString());
                System.out.println(charTermAttribute.toString()+" "+offsetAttribute.startOffset()+"-"+offsetAttribute.endOffset());
            }
            System.out.println("1 " + String.join(" ", expected));
            System.out.println("2 " + String.join(" ", termList) + "\n");
            assertThat(termList, is(expected));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
    
    @Test
    public void highlightTest() throws ParseException, IOException, InvalidTokenOffsetsException {
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("s", "e");
        final String originalField = "丹珠爾大藏經";
        final String queryString = "\"zhū\"";
        Analyzer indexAnalyzer = new ChineseAnalyzer("TC2PYstrict", false, 0);
        Analyzer queryAnalyzer = new ChineseAnalyzer("PYstrict");
        QueryParser queryParser = new QueryParser("", queryAnalyzer) ;
        queryParser.setAllowLeadingWildcard(true) ;
        Query query = queryParser.parse(queryString) ;
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(20));
        TokenStream tokenStream = indexAnalyzer.tokenStream("", originalField);
        TextFragment[] frags = highlighter.getBestTextFragments(tokenStream, originalField, true, 20);
        for (final TextFragment f : frags) {
            System.out.println(f.toString());
        }
        indexAnalyzer.close();
        queryAnalyzer.close();
    }
}
