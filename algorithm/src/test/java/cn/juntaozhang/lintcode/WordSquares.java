package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class WordSquares {
    List<List<String>> res = new ArrayList<>();
    int len = -1;
    Map<String, List<String>> dict = new HashMap<>();
    String[] words;

    @Test
    public void wordSquares() {
        System.out.println(wordSquares(new String[]{"area", "lead", "wall", "lady", "ball"}));
    }

    public List<List<String>> wordSquares(String[] words) {
        if (words.length == 0) {
            return res;
        }
        this.words = words;
        this.len = words[0].length();
        for (String w : words) {
            for (int i = 1; i < len; i++) {
                String key = w.substring(0, i);
                List<String> t = dict.get(key);
                if (t == null) {
                    t = new ArrayList<>();
                    dict.put(key, t);
                }
                t.add(w);
            }
        }

        dfs(0, new ArrayList<String>());

        return res;
    }

    private void dfs(int index, List<String> last) {
        if (last.size() == len) {
            res.add(last);
            return;
        }

        if (last.size() == 0) {
            for (String w : words) {
                List<String> t = new ArrayList<String>();
                t.add(w);
                dfs(index + 1, t);
            }
        } else {
            List<String> t = dict.get(getNextPrefix(last));
            if (t == null) {
                return;
            }
            for (String w : t) {
                List<String> t2 = new ArrayList<String>(last);
                t2.add(w);
                dfs(index + 1, t2);
            }
        }
    }

    private String getNextPrefix(List<String> last) {
        StringBuilder prefix = new StringBuilder();
        for (String str : last) {
            prefix.append(str.charAt(last.size()));
        }
        return prefix.toString();
    }
}
