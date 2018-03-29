package io.bdrc.lucene.zh;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
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
 *      Implements the algorithm presented @see <a href="https://github.com/tsroten/zhon/blob/develop/zhon/pinyin.py#L64">here</a>:
 *      1. get the longest valid syllable (in the 421 syllables from @see <a href="https://en.wikipedia.org/wiki/Pinyin_table#Overall_table">Pinyin Table</a> 
 *      2. If it ends in a consonant make sure it's not followed directly by a
 *         vowel (hyphens and apostrophes don't count).
 *      3. If the above didn't match, repeat for the next longest valid match.
 *      note: as found in the linked table, the only valid ending consonants are 'n' and 'g'
 *      
 * Syllable breaks:
 * 
 *      Apostrophes and hyphens always induce a syllable break.
 *      They are expected in the cases not covered by the described segmenting algorithm.
 *      
 *      "changan" yields "chan", "gan"
 *      "chang'an" and "chang-an" yield "chang", "an"
 * 
 * Note: As explained in @see <a href="https://en.wikipedia.org/wiki/Erhua">this article</a>, the erhua phenomenon pertains to the
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
    PinyinSyllableTokenizer () throws FileNotFoundException, IOException {
        init();
    }
    
    private int bufferIndex = 0, finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    
    private RollingCharBuffer ioBuffer;
    private Row rootRow;
    private Row currentRow;
    private int tokenLength;
    private int tokenStart;
    private int tokenEnd;
    
    private static final List<Character> unihanPinyinDiacritics = Arrays.asList(
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
            'Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü',
            'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü');
    private static final List <Character> pinyinVowels = Arrays.asList('a', 'e', 'i', 'o', 'u', 'v', 'ü');
    private static final List<Character> pinyinNumbers = Arrays.asList('0', '1', '2', '3', '4', '5');
    private StringCharacterIterator nonwordIterator = null;
    private int nonwordOffset = -1;
    
    boolean debug = false;
    
    /**
     * 
     * @throws FileNotFoundException  the file of the compiled Trie is not found
     * @throws IOException  the file of the compiled Trie can't be opened
     */
    private void init() throws FileNotFoundException, IOException {
        InputStream stream = null;
        stream = PinyinSyllableTokenizer.class.getResourceAsStream("/zh_py-compiled-trie.dump");
        if (stream == null) {  // we're not using the jar, there is no resource, assuming we're running the code
            String compiledTrieName = "src/main/resources/zh_py-compiled-trie.dump";
            if (!new File(compiledTrieName).exists()) {
                System.out.println("The default compiled Trie is not found ; building it will take some time!");
                long start = System.currentTimeMillis();
                this.scanner = BuildCompiledTrie.compileTrie();
                long end = System.currentTimeMillis();
                System.out.println("Trie built in " + (end - start) / 1000 + "s.");
                ioBuffer = new RollingCharBuffer();
                ioBuffer.reset(input);
            } else {
                init(new FileInputStream(compiledTrieName));    
            }   
        } else {
            init(stream);
        }
    }
    
    /**
     * Opens an existing compiled Trie
     * 
     * @param inputStream the compiled Trie opened as a Stream 
     */
    private void init(InputStream inputStream) throws FileNotFoundException, IOException {
        this.scanner = new Trie(new DataInputStream(inputStream));
        ioBuffer = new RollingCharBuffer();
        ioBuffer.reset(input);
    }
    
    /**
     * Returns true iff a codepoint should be included in a token. This tokenizer
     * generates as tokens adjacent sequences of codepoints which satisfy this
     * predicate. Codepoints for which this is false are used to define token
     * boundaries and are not included in tokens.
     * 
     * @param c the current character
     * @return true iff c is a valid Pinyin character.
     */
    protected boolean isTokenChar(int c) {
        return (c > 96 && c < 123) || (c > 64 && c < 91) 
                || unihanPinyinDiacritics.contains((char) c)
                || pinyinVowels.contains((char) c)
                || pinyinNumbers.contains((char) c);
    }

    /**
     * Lowercases all the token chars (filtering non-Pinyin accents is done by PinyinNormalizingFilter)
     * 
     * @param c the codepoint to normalize (current char)
     * @return the lower-cased codepoint
     */
    protected int normalize(int c) {
        return Character.toLowerCase(c);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (nonwordIterator != null) {
            char next = nonwordIterator.next();
            if (next != CharacterIterator.DONE) {
                termAtt.setEmpty().append(next);
                nonwordOffset ++;
                offsetAtt.setOffset(correctOffset(nonwordOffset), correctOffset(nonwordOffset + 1));
                typeAtt.setType("non-word");
                return true;
            } else {
                nonwordIterator = null;
                nonwordOffset = -1;
            }
        }
        
        if (debug) {System.out.println("----------");}
        
        clearAttributes();
        ioBuffer.freeBefore(bufferIndex);
        
        currentRow = null;
        rootRow = scanner.getRow(scanner.getRoot());
        
        char[] tokenBuffer = termAtt.buffer();
        tokenStart = bufferIndex;
        tokenEnd = -1;
        tokenLength = 0;
        
        boolean match = false;
        boolean continuing = false;
        boolean hasMatched = false;
        int longestMatchIdx = -1;
        
        while (true) {
            final int c = normalize(ioBuffer.get(bufferIndex));    // take next char in ioBuffer and normalize it
            bufferIndex ++;
            /* when ioBuffer is empty (end of input, ...) */
            if (c == -1) {
                bufferIndex --;
                if (tokenLength == 0) {
                    finalOffset = correctOffset(bufferIndex);
                    return false;
                }
                break;
            }
            if (debug) {System.out.println("\t" + (char) c);}
            
            if (isTokenChar(c)) {               // if it's a token char
                
                /* start of syllable or non-syl char */
                if (tokenLength == 0) {
                    match = tryToFindMatchIn(rootRow, c);
                    continuing = tryToContinueDownTheTrie(rootRow, c);
                    
                } else if (continuing){
                    match = tryToFindMatchIn(currentRow, c);
                    continuing = tryToContinueDownTheTrie(currentRow, c);                    
                }
                /* there was a match */
                if (!hasMatched && match) {
                    hasMatched = true;
                }
                
                /* reached the longest match */
                if (longestMatchIdx == -1 && hasMatched && !continuing) {
                    longestMatchIdx = bufferIndex;
                }
                
                tokenEnd = bufferIndex;  // has to be incremented before breaking
                int lastCharIdx = (termAtt.length() - 1 < 0) ? 0: termAtt.length() - 1;
                
                /* current char does not belong to the current syllable
                 * (current char is a vowel and previous letter is 'g' or 'n') */  
                if (hasMatched && lastCharIdx > 0 && (pinyinVowels.contains((char) c) || unihanPinyinDiacritics.contains((char) c)) 
                            && (termAtt.length() > 0 && 
                                    (tokenBuffer[lastCharIdx] == 'g' || tokenBuffer[lastCharIdx] == 'n'))) {
                        tokenLength --;
                        bufferIndex -= 2;
                        break;
                                 
//                /* it is a non-word: return a non-word token containing the current char */
//                } else if (!match && !continuing) {
//                        IncrementTokenLengthAndAddCurrentCharTo(tokenBuffer, c);
//                        break;
               
                /* reached the end of the longest syllable or a non-syllable */
                } else if (!continuing){                        
                    if (!match) {
                        if (!hasMatched && tokenLength == 1) {
                            bufferIndex --;
                            break;
                        
                        } else if (!hasMatched && tokenLength > 1) {
                            IncrementTokenLengthAndAddCurrentCharTo(tokenBuffer, c);
                            break;
//
//                        } else if (hasMatched && unihanPinyinDiacritics.contains((char) c)) {
//                            break;
               
                        } else if (hasMatched && tokenLength >= 1) {
                            bufferIndex --;
                            break;
                        
                        } else {
                            IncrementTokenLengthAndAddCurrentCharTo(tokenBuffer, c);
                            break;
                        }
                    } else if (bufferIndex >= longestMatchIdx) {
                        if (pinyinVowels.contains((char) c) || unihanPinyinDiacritics.contains((char) c)) { 
                            IncrementTokenLengthAndAddCurrentCharTo(tokenBuffer, c);
                            break;
                        } else if (bufferIndex > longestMatchIdx) {
                            bufferIndex --;
                            break;
                        }
                    }
                }
                
                /* still building the current syllable */
                IncrementTokenLengthAndAddCurrentCharTo(tokenBuffer, c);
                
                if (tokenLength >= MAX_WORD_LEN) { // buffer overflow! make sure to check for >= surrogate pair could break == test
                    break;
                }
            } else if (tokenLength > 0) {           // at non-Letter w/ chars
                break;                           // return 'em
            }
        }

        if (!hasMatched) {
            typeAtt.setType("non-word");
            
            /* non-word has more than one character */
            if (termAtt.length() > 1) {
                nonwordIterator  = new StringCharacterIterator(termAtt.toString());
                termAtt.setLength(1);
                nonwordOffset = tokenStart;
                offsetAtt.setOffset(correctOffset(tokenStart), correctOffset(tokenStart + 1));
                return true;
            }
        }
        termAtt.setLength(tokenLength);
        assert tokenStart != -1;
        offsetAtt.setOffset(correctOffset(tokenStart), correctOffset(tokenEnd));
        return true;
    }
    
    private boolean tryToContinueDownTheTrie(Row row, int c) {
        int ref = row.getRef((char) c);
        currentRow = (ref >= 0) ? scanner.getRow(ref) : null;
        return (currentRow == null) ? false: true;
    }
    
    private boolean tryToFindMatchIn(Row row, int c) {
        int cmdIndex = row.getCmd((char) c);
        return cmdIndex >= 0;
    }
    
    private void IncrementTokenLengthAndAddCurrentCharTo(char[] tokenBuffer, int c) {
        tokenLength += Character.toChars(normalize(c), tokenBuffer, tokenLength);   // add normalized c to tokenBuffer
        termAtt.setLength(tokenLength);
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
        finalOffset = 0;
        ioBuffer.reset(input); // make sure to reset the IO buffer!!
    }
}
