package cn.juntaozhang.lintcode.HighFrequent;

import org.junit.Test;

import java.util.*;

/**
 * Interview Style of Google/Facebook
 * //http://www.lintcode.com/zh-cn/ladder/14/
 *
 * 
 */
public class HighFrequent1 {
    //================================================Strings Homomorphism==============================================
    /**
     * @param s a string
     * @param t a string
     * @return true if the characters in s can be replaced to get t or false
     */
    public boolean isIsomorphic2(String s, String t) {
        if (s == null && t == null) {
            return true;
        }
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        Map<Character, Character> map = new HashMap<>();// 能用int[]替换
        Map<Character, Character> map2 = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            if (map.get(s.charAt(i)) == null && map2.get(t.charAt(i)) == null) {
                map.put(s.charAt(i), t.charAt(i));
                map2.put(t.charAt(i), s.charAt(i));
            } else if (
                    map.get(s.charAt(i)) == null || map2.get(t.charAt(i)) == null
                            || map.get(s.charAt(i)) != t.charAt(i) || map2.get(t.charAt(i)) != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIsomorphic(String s, String t) {
        int[] tm = new int[128];
        int[] sm = new int[128];
        for (int i = 0; i < s.length(); i++) {
            int tc = (int) t.charAt(i);
            int sc = (int) s.charAt(i);
            if (tm[tc] == 0 && sm[sc] == 0) {
                tm[tc] = sc;
                sm[sc] = tc;
            } else if (tm[tc] != sc || sm[sc] != tc) {
                return false;
            }
        }
        return true;
    }

    //==============================================Check Word Abbreviation=============================================
    public boolean validWordAbbreviation2(String word, String abbr) throws Exception {
        String t = "";
        for (int i = 0, j = 0; i < word.length(); i++) {
            //获取数字
            while ((int) abbr.charAt(j) > '0' && (int) abbr.charAt(j) < '9') {
                t += abbr.charAt(j);
                j++;
            }
            if (t != "") {
                i += Integer.valueOf(t);//这里需要用int替换
                t = "";
                continue;
            }
            if (word.charAt(i) != abbr.charAt(j)) {
                return false;
            }
        }
        Map<String, List<Integer>> map = new HashMap<>();
        return true;
    }

    /**
     * @param word a non-empty string
     * @param abbr an abbreviation
     * @return true if string matches with the given abbr or false
     */
    public boolean validWordAbbreviation(String word, String abbr) {
        int t = 0;
        for (int i = 0, j = 0; i < word.length();) {
            //如果开头不是‘0’
            if (abbr.charAt(j) != '0') {
                //获取数字
                while (j < abbr.length() && Character.isDigit(abbr.charAt(j))) {
                    t = t * 10 + (abbr.charAt(j) - '0');
                    j++;
                }
                if (t != 0) {
                    i += t;
                    if (i > word.length()) {
                        return false;
                    }
                    t = 0;
                    continue;
                }
            }
            if (word.charAt(i) != abbr.charAt(j)) {
                return false;
            }
            i++;
            j++;
        }
        return true;
    }

    @Test
    public void validWordAbbreviation() {
        System.out.println(validWordAbbreviation("aaudrjtossfxxsgiuimqqblazarmwymxzaxxoiyjxdcqfgofqrgypuzqdavftqtzyeqglgnyyrxykwcfqozmgeylrdebgapgevllyzbsbiofnkdelicpajzrxkjfapmpturjfumygqrgmjkymnzzbcoieinlcecndbbpnerurlzmqvifaoobaqgmxxeftsztbnfeskrercftlvaeisqieldlsqlocbubjxbuebzmjcrsgrmrzwwbldgbtldvmexuenfrvenfjkikzlkogbrdbabdcydrpgcwasmgkpyslyabbriaewtnouskyzqztrtenljibcirvdnbjmfvgvcybvltzfdvoytslooxmmxtaofqbqokuemrgidgqcdnweotcyibxddbuvnfxkxaofzgasztgpicoetujkefuemarszrkvlurapduoonkoiyaidoggpfspfymwxzigkfsieiftzazdlcxskojyjnqcpafkoepfordwqxriiwocyoqiozmaxmqonuegefjwxpoiydbtmergdxcoaqlaxdjfdcoakcnjswunkvgztiyvztuabcmtescmogyqnpgevrxopjxdesyiclenetmpaplvmjcyekxnvkylckwmnayfxfctcjpssjyibsjeoogcklbujxjeessfidlagefljpjoxkowjokalmdmakamsesqfzdatxokwifmsfmxsplojcpygcccrlwpyetkbyfmkqlnrcqprmyalxjkowqctabryrmloswtrjvdufmjvtxyjezodbdxzuuswfbmpplypsjuafzalfwpspkrzkqxbicuwwzysbipugvtcyseexopxgdifcxjmdwwwwwwwwww", "2u852d10"));
        System.out.println(validWordAbbreviation("a", "1"));
    }

    //=====================================================矩形重叠======================================================
    /**
     * Definition for a point.
     */
     class Point {
         public int x, y;
         public Point() { x = 0; y = 0; }
         public Point(int a, int b) { x = a; y = b; }
     }

    /**
     * @param l1 top-left coordinate of first rectangle
     * @param r1 bottom-right coordinate of first rectangle
     * @param l2 top-left coordinate of second rectangle
     * @param r2 bottom-right coordinate of second rectangle
     * @return true if they are overlap or false
     */
    public boolean doOverlap(Point l1, Point r1, Point l2, Point r2) {
        if (l2.x > r1.x || r2.x < l1.x || l2.y < r1.y || r2.y > l1.y) {
            return false;
        }
        return true;
    }

    //=====================================================解码方法======================================================
    /**
     * @param s a string,  encoded message
     * @return an integer, the number of ways decoding
     */
    public int numDecodings(String s) {
        if (s == null || "".equals(s)) {
            return 0;
        }
        int[] n = new int[s.length() + 1];
        n[0] = 1;
        n[1] = s.charAt(0) == '0' ? 0 : 1;
        for (int i = 2; i <= s.length(); i++) {
            char c = s.charAt(i - 1);
            int t = (s.charAt(i - 2) - '0') * 10 + (c - '0');
            if (c != '0') {
                if (t <= 26 && t > 10){
                    n[i] = n[i - 1] + n[i - 2];
                } else {
                    n[i] = n[i - 1];//特别注意1001，101
                }
            } else {
                if (t >= 10 && t <= 26) {
                    n[i] = n[i - 2];
                }
            }
        }
        return n[s.length()];
    }

    //================================================Words Abbreviation================================================
    public String[] wordsAbbreviation(String[] dict) {
        int[] count = new int[dict.length];
        Map<String,List<Integer>> cache = new HashMap<>();
        int max = -1;
        for (int c = 1; c < max || max == -1; c++) {
            for (int i = 0; i < dict.length; i++) {
                String word = dict[i];
                if(c == 1) {
                    max = Math.max(max,word.length());
                }
                if (word.length() <= c + 2) {
                    continue;
                }
                if (count[i] != 0){
                    continue;
                }
                String key = word.substring(0,c) + String.valueOf(word.length() - c -1) + word.charAt(word.length() - 1);
                List<Integer> list = cache.get(key);
                if (list == null) {
                    list = new ArrayList<Integer>();
                    cache.put(key,list);
                }
                list.add(i);
            }
            for (String key: cache.keySet()) {
                List<Integer> ls = cache.get(key);
                if(ls.size() == 1){
                    Integer index = ls.get(0);
                    count[index] = 1;
                    dict[index] = key;
                }
            }
            cache.clear();
        }
        return dict;
    }

    @Test
    public void wordsAbbreviation() {
        System.out.println(Arrays.toString(wordsAbbreviation(
                new String[]{"like", "god", "internal", "me", "internet", "interval", "intension", "face", "intrusion"}
        )));
    }
}
