package io.bdrc.lucene.zh;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.RollingCharBuffer;

import io.bdrc.lucene.stemmer.Row;
import io.bdrc.lucene.stemmer.Trie;

/**
 * Tokenizes any valid Pinyin text into syllables.
 * Pinyin can indifferently be numbered Pinyin, marked Pinyin and lazy Pinyin
 * or a mixture of any of them.
 * 
 * Segmenting algorithm:
 *      
 *      Implements the algorithm presented {@link https://github.com/tsroten/zhon/blob/develop/zhon/pinyin.py#L64}:
 *      1. get the longest valid syllable (in the 421 syllables from {@link https://en.wikipedia.org/wiki/Pinyin_table#Overall_table} 
 *      2. If it ends in a consonant make sure it's not followed directly by a
 *         vowel (hyphens and apostrophes don't count).
 *      3. If the above didn't match, repeat for the next longest valid match.
 *      
 * Syllable breaks:
 * 
 *      Apostrophes and hyphens always induce a syllable break.
 *      They are expected in the cases not covered by the described segmenting algorithm.
 *      
 *      "changan" yields "chan", "gan"
 *      "chang'an" and "chang-an" yield "chang", "an"
 * 
 * Note: As explained {@link http://www.thefullwiki.org/Erhua}, the erhua phenomenon pertains to the
 *      spoken Mandarin dialect and to some Northern dialects. As such, we don's support it since
 *      we aim to index literary Chinese. "tangr" yields "tang", "r"
 * 
 * @author Hélios Hildt
 *
 */
public class PinyinSyllableTokenizer extends Tokenizer{
    private Trie scanner;
    
    /**
     * 
     * @throws FileNotFoundException 
     * @throws IOException
     */
    PinyinSyllableTokenizer () {
        init();
    }
    
    private int offset = 0, bufferIndex = 0, finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    
    private RollingCharBuffer ioBuffer;
    private final int charCount = 1; // the number of chars in a codepoint
    private Row rootRow;
    private Row currentRow;
    
    private void init() {
        scanner = BuildCompiledTrie.buildTrie();
        ioBuffer = new RollingCharBuffer();
        ioBuffer.reset(input);
    }
    
    /**
     * Returns true iff a codepoint should be included in a token. This tokenizer
     * generates as tokens adjacent sequences of codepoints which satisfy this
     * predicate. Codepoints for which this is false are used to define token
     * boundaries and are not included in tokens.
     */
    protected boolean isTokenChar(int c) {
        List<Character> unihanPinyinDiacritics = Arrays.asList(
                'Ā', 'Á', 'Ǎ', 'À', 
                'ā', 'á', 'ǎ', 'à', 
                'Ē', 'É', 'Ě', 'È', 
                'ē', 'é', 'ě', 'è', 
                'Ī', 'Í', 'Ǐ', 'Ì', 
                'ī', 'í', 'ǐ', 'ì', 
                'Ō', 'Ó', 'Ǒ', 'Ò', 
                'ō', 'ó', 'ǒ', 'ò', 
                'Ū', 'Ú', 'Ǔ', 'Ù', 
                'ū', 'ú', 'ǔ', 'ù', 
                'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ',
                'ǖ', 'ǘ', 'ǚ', 'ǜ', 
                // numbers for numbered Pinyin
                '0', '1', '2', '3', '4', '5');
        return (c > 96 && c < 123) || (c > 64 && c < 91) || unihanPinyinDiacritics.contains((char) c);
    }

    /**
     * Lowercases all the token chars (filtering non-Pinyin accents is done by PinyinNormalizingFilter)
     */
    protected int normalize(int c) {
        return Character.toLowerCase(c);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        ioBuffer.freeBefore(bufferIndex);
        rootRow = scanner.getRow(scanner.getRoot());
        int length = 0;
        int start = -1; // this variable is always initialized
        int end = -1;
        currentRow = null;
        char[] buffer = termAtt.buffer();
        
        while (true) {
            final int c = normalize(ioBuffer.get(bufferIndex));    // take next char in ioBuffer and normalize it
            bufferIndex += charCount;                   // increment bufferIndex for next value of c

            if (isTokenChar(c)) {               // if it's a token char
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length-1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2+length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it, normalized
                if (length >= MAX_WORD_LEN) { // buffer overflow! make sure to check for >= surrogate pair could break == test
                    break;
                }
            } else if (c == '\'') {
              // check 
            } else if (length > 0) {           // at non-Letter w/ chars
                break;                           // return 'em
            }
        }

        termAtt.setLength(length);
        assert start != -1;
        offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
        return true;
    }
    
    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        bufferIndex = 0;
        offset = 0;
        finalOffset = 0;
        ioBuffer.reset(input); // make sure to reset the IO buffer!!
    }
}
