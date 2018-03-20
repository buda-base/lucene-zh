package io.bdrc.lucene.zh;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.bdrc.lucene.stemmer.Trie;

public class BuildCompiledTrie {
    

    
    public static void main(String [] args){
        
    }
    
    /** 
     * Builds a trie with all valid Pinyin syllables, adding the v-alternatives to syllables containing ü.
     * 
     * Uses the list of valid syllables from {@link https://en.wikipedia.org/wiki/Pinyin_table#Overall_table}
     * and {@link PinyinNumberedToMarkedFilter#numberedToMarked} to generate the marked syllables
     * 
     * for every syllable, the following variants are generated:
     *          - lazy Pinyin (syllable as-is)
     *          - for each of the four tones:
     *              - a numbered version
     *              - a marked version
     * 
     * @return the non-optimized Trie
     */
    static Trie buildTrie() throws FileNotFoundException, IOException {
        List<String> validSyllables = Arrays.asList("zhi", "chi", "shi", "ri", "zi", "ci", "si",
                "a", "ba", "pa", "ma", "fa", "da", "ta", "na", "la", "ga", "ka", "ha", "zha", "cha", "sha", "za", "ca", "sa",
                "o", "bo", "po", "mo", "fo", "lo",
                "e", "me", "de", "te", "ne", "le", "ge", "ke", "he", "zhe", "che", "she", "re", "ze", "ce", "se",
                "ê",
                "ai", "bai", "pai", "mai", "dai", "tai", "nai", "lai", "gai", "kai", "hai", "zhai", "chai", "shai", "zai", "cai", "sai",
                "ei", "bei", "pei", "mei", "fei", "dei", "tei", "nei", "lei", "gei", "kei", "hei", "zhei", "shei", "zei", "sei",
                "ao", "bao", "pao", "mao", "dao", "tao", "nao", "lao", "gao", "kao", "hao", "zhao", "chao", "shao", "rao", "zao", "cao", "sao",
                "ou", "pou", "mou", "fou", "dou", "tou", "nou", "lou", "gou", "kou", "hou", "zhou", "chou", "shou", "rou", "zou", "cou", "sou",
                "an", "ban", "pan", "man", "fan", "dan", "tan", "nan", "lan", "gan", "kan", "han", "zhan", "chan", "shan", "ran", "zan", "can", "san",
                "en", "ben", "pen", "men", "fen", "den", "nen", "gen", "ken", "hen", "zhen", "chen", "shen", "ren", "zen", "cen", "sen",
                "ang", "bang", "pang", "mang", "fang", "dang", "tang", "nang", "lang", "gang", "kang", "hang", "zhang", "chang", "shang", "rang", "zang", "cang", "sang",
                "eng", "beng", "peng", "meng", "feng", "deng", "teng", "neng", "leng", "geng", "keng", "heng", "zheng", "cheng", "sheng", "reng", "zeng", "ceng", "seng",
                "er",
                "yi", "bi", "pi", "mi", "di", "ti", "ni", "li", "ji", "qi", "xi",
                "ya", "dia", "nia", "lia", "jia", "qia", "xia",
                "yo",
                "ye", "bie", "pie", "mie", "die", "tie", "nie", "lie", "jie", "qie", "xie",
                "yai",
                "yao", "biao", "piao", "miao", "fiao", "diao", "tiao", "niao", "liao", "jiao", "qiao", "xiao",
                "you", "miu", "diu", "niu", "liu", "jiu", "qiu", "xiu",
                "yan", "bian", "pian", "mian", "dian", "tian", "nian", "lian", "jian", "qian", "xian",
                "yin", "bin", "pin", "min", "nin", "lin", "jin", "qin", "xin",
                "yang", "biang", "diang", "niang", "liang", "jiang", "qiang", "xiang",
                "ying", "bing", "ping", "ming", "ding", "ting", "ning", "ling", "jing", "qing", "xing",
                "wu", "bu", "pu", "mu", "fu", "du", "tu", "nu", "lu", "gu", "ku", "hu", "zhu", "chu", "shu", "ru", "zu", "cu", "su",
                "wa", "gua", "kua", "hua", "zhua", "chua", "shua", "rua",
                "wo", "duo", "tuo", "nuo", "luo", "guo", "kuo", "huo", "zhuo", "chuo", "shuo", "ruo", "zuo", "cuo", "suo",
                "wai", "guai", "kuai", "huai", "zhuai", "chuai", "shuai",
                "wei", "dui", "tui", "gui", "kui", "hui", "zhui", "chui", "shui", "rui", "zui", "cui", "sui",
                "wan", "duan", "tuan", "nuan", "luan", "guan", "kuan", "huan", "zhuan", "chuan", "shuan", "ruan", "zuan", "cuan", "suan",
                "wen", "dun", "tun", "nun", "lun", "gun", "kun", "hun", "zhun", "chun", "shun", "run", "zun", "cun", "sun",
                "wang", "guang", "kuang", "huang", "zhuang", "chuang", "shuang",
                "weng", "dong", "tong", "nong", "long", "gong", "kong", "hong", "zhong", "chong", "shong", "rong", "zong", "cong", "song",
                "yu", "nü", "lü", "ju", "qu", "xu", 
                "yue", "nüe", "lüe", "jue", "que", "xue",
                "yuan", "juan", "quan", "xuan",
                "yun", "lün", "jun", "qun", "xun",
                "yong", "jiong", "qiong", "xiong",
                // alternatives to syllables containing ü with v instead
                "nv", "lv", "nve", "lve", "lvn");
        
        Trie trie = new Trie(true);
        for (String syl: validSyllables) {
            trie.add(syl, "");
            for (int i = 1; i<= 4; i++) {
                String numbered = syl + i;
                trie.add(numbered, "");
                trie.add(PinyinNumberedToMarkedFilter.numberedToMarked(numbered), "");
            }
        }
        return trie;
    }
    
}
