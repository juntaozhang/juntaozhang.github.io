package cn.juntaozhang.leetcode.test1;

import org.junit.Assert;
import org.junit.Test;

public class L211 {

    @Test
    public void case1() {
        WordDictionary wordDictionary = new WordDictionary();
        wordDictionary.addWord("bad");
        wordDictionary.addWord("dad");
        wordDictionary.addWord("mad");
        Assert.assertFalse(wordDictionary.search("pad")); // 返回 False
        Assert.assertTrue(wordDictionary.search("bad")); // 返回 True
        Assert.assertTrue(wordDictionary.search(".ad")); // 返回 True
        Assert.assertTrue(wordDictionary.search("b..")); // 返回 True
    }

    class WordDictionary {
        Trie root;

        public WordDictionary() {
            root = new Trie();
        }

        public void addWord(String word) {
            root.insert(word);
        }

        public boolean search(String word) {
            return dfs(word, 0, root);
        }

        private boolean dfs(String word, int i, Trie node) {
            if (i == word.length()) {
                return node.isEnd();
            }
            char c = word.charAt(i);
            Trie[] children = node.getChildren();
            if (c != '.') {
                if (children[c - 'a'] != null && dfs(word, i + 1, children[c - 'a'])) {
                    return true;
                }
            } else {
                for (int j = 0; j < 26; j++) {
                    if (children[j] != null && dfs(word, i + 1, children[j])) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    class Trie {
        private Trie[] children;
        private boolean isEnd;

        public Trie() {
            children = new Trie[26];
            isEnd = false;
        }

        public void insert(String word) {
            Trie node = this;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                int index = ch - 'a';
                if (node.children[index] == null) {
                    node.children[index] = new Trie();
                }
                node = node.children[index];
            }
            node.isEnd = true;
        }

        public Trie[] getChildren() {
            return children;
        }

        public boolean isEnd() {
            return isEnd;
        }
    }
}
