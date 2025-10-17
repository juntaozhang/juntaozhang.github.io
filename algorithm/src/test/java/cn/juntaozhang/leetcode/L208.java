package cn.juntaozhang.leetcode;

/**
 * 
 */
public class L208 {
    class Trie {
        class TrieNode {
            TrieNode[] links = new TrieNode[26];
            private boolean isEnd;

            TrieNode get(char c) {
                return links[c - 'a'];
            }

            TrieNode put(char c, TrieNode node) {
                links[c - 'a'] = node;
                return node;
            }

            void setEnd() {
                this.isEnd = true;
            }

            boolean isEnd() {
                return isEnd;
            }

            @Override
            public String toString() {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < links.length; i++) {
                    if (links[i] != null) {
                        s.append((char) (i + 'a'));
                    }
                }
                s.append(" isEnd:").append(isEnd);
                return s.toString();
            }
        }

        private TrieNode root;

        /**
         * Initialize your data structure here.
         */
        public Trie() {
            root = new TrieNode();
        }

        /**
         * Inserts a word into the trie.
         */
        public void insert(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                TrieNode t = node.get(c);
                if (t == null) {
                    t = node.put(c, new TrieNode());
                }
                node = t;
            }
            node.setEnd();
        }

        private TrieNode searchNode(String word) {
            TrieNode node = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                node = node.get(c);
                if (node == null) {
                    return null;
                }
            }
            return node;
        }

        /**
         * Returns if the word is in the trie.
         */
        public boolean search(String word) {
            TrieNode node = searchNode(word);
            return node != null && node.isEnd();
        }

        /**
         * Returns if there is any word in the trie that starts with the given prefix.
         */
        public boolean startsWith(String prefix) {
            return searchNode(prefix) != null;
        }
    }

    public static void main(String[] args) {
        Trie trie = new L208().new Trie();
//        String[] b = new String[]{"insert", "insert", "insert", "insert", "insert", "insert", "search", "search", "search", "search", "search", "search", "search", "search", "search", "startsWith", "startsWith", "startsWith", "startsWith", "startsWith", "startsWith", "startsWith", "startsWith", "startsWith"};
//        String[] a = new String[]{"app", "apple", "beer", "add", "jam", "rental", "apps", "app", "ad", "applepie", "rest", "jan", "rent", "beer", "jam", "apps", "app", "ad", "applepie", "rest", "jan", "rent", "beer", "jam"};

        String[] b = new String[]{"insert", "search", "search", "startsWith", "insert", "search"};
        String[] a = new String[]{"apple", "apple", "app", "app", "app", "app"};
        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + "=>" + a[i] + " ");
            if ("ad".equals(a[i])) {
                System.out.print("");
            }
            if ("insert".equals(b[i])) {
                trie.insert(a[i]);
                System.out.println("null");
            } else if ("search".equals(b[i])) {
                System.out.println(trie.search(a[i]));
            } else {
                System.out.println(trie.startsWith(a[i]));
            }
        }
    }
}
