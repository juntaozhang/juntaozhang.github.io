package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class L355_Twitter {
    // follow 只能看到关注之后的内容
    static class Twitter1 {
        private final Map<Integer, List<Integer>> userIdFollowsMap;
        private final Map<Integer, LinkedList<Integer>> userIdNewsFeedsMap;
        private final Map<Integer, LinkedList<Integer>> ownerNewsFeedsMap;

        public Twitter1() {
            // map userid -> followerIds for follow and unfollow
            userIdFollowsMap = new HashMap<>();
            // map userid/followeeid -> tweetIds(queue)
            userIdNewsFeedsMap = new HashMap<>();
            // map for owner -> tweetIds
            ownerNewsFeedsMap = new HashMap<>();
        }

        public void postTweet(int userId, int tweetId) {
            // add feed which post by self + followee
            addNewsFeed(userId, tweetId);
            // only add feed which post by yourself
            LinkedList<Integer> feeds = ownerNewsFeedsMap.get(userId);
            if (feeds == null) {
                feeds = new LinkedList<>();
                ownerNewsFeedsMap.put(userId, feeds);
            }
            feeds.offer(tweetId);
            if (feeds.size() > 10) {
                feeds.poll();// drop old new
            }

            // add follows newFeed
            List<Integer> follows = userIdFollowsMap.get(userId);
            if (follows != null) {
                for (Integer follow : follows) {
                    addNewsFeed(follow, tweetId);
                }
            }
        }

        private void addNewsFeed(int userId, int tweetId) {
            LinkedList<Integer> newsFeeds = userIdNewsFeedsMap.get(userId);
            if (newsFeeds == null) {
                newsFeeds = new LinkedList<>();
                userIdNewsFeedsMap.put(userId, newsFeeds);
            }
            newsFeeds.offer(tweetId);
            if (newsFeeds.size() > 10) {
                newsFeeds.poll();// drop old new
            }
        }

        public List<Integer> getNewsFeed(int userId) {
            LinkedList<Integer> feeds = userIdNewsFeedsMap.getOrDefault(userId, new LinkedList<>());
            List<Integer> res = new ArrayList<>();
            ListIterator<Integer> itor = feeds.listIterator(feeds.size());
            while (itor.hasPrevious()) {
                res.add(itor.previous());
            }
            return res;
        }

        public void follow(int followerId, int followeeId) {
            List<Integer> follows = userIdFollowsMap.get(followeeId);
            if (follows == null) {
                follows = new ArrayList<>();
                userIdFollowsMap.put(followeeId, follows);
            }
            if (!follows.contains(followerId)) {
                follows.add(followerId);
            }
        }

        public void unfollow(int followerId, int followeeId) {
            List<Integer> follows = userIdFollowsMap.get(followeeId);
            if (follows != null) {
                follows.remove((Object) followerId);

                // remove tweetIds
                LinkedList<Integer> followeeFeeds = ownerNewsFeedsMap.get(followeeId);
                LinkedList<Integer> myFeeds = userIdNewsFeedsMap.get(followerId);
                if (followeeFeeds != null && myFeeds != null) {
                    for (Integer feed : followeeFeeds) {
                        myFeeds.removeIf(integer -> Objects.equals(integer, feed));
                    }
                }
            }
        }
    }

    static class Twitter {
        private final Map<Integer, List<Integer>> userIdFollowsMap;
        private final Map<Integer, PriorityQueue<int[]>> userIdNewsFeedsMap;
        private final Map<Integer, LinkedList<int[]>> ownerNewsFeedsMap;
        private int time;
        private int limit;

        public Twitter() {
            // map userid -> followerIds for follow and unfollow
            userIdFollowsMap = new HashMap<>();
            // map userid/followeeid -> tweetIds(queue)
            userIdNewsFeedsMap = new HashMap<>();
            // map for owner -> tweetIds
            ownerNewsFeedsMap = new HashMap<>();
            time = 0;
            limit = 1000;
        }

        public void postTweet(int userId, int tweetId) {
            time++;
            // add feed which post by self + followee
            addNewsFeed(userId, tweetId);
            // only add feed which post by yourself
            LinkedList<int[]> feeds = ownerNewsFeedsMap.get(userId);
            if (feeds == null) {
                feeds = new LinkedList<>();
                ownerNewsFeedsMap.put(userId, feeds);
            }
            feeds.offer(new int[]{tweetId, time});
            if (feeds.size() > limit) {
                feeds.poll();// drop old new
            }

            // add follows newFeed
            List<Integer> follows = userIdFollowsMap.get(userId);
            if (follows != null) {
                for (Integer follow : follows) {
                    addNewsFeed(follow, tweetId);
                }
            }
        }

        private void addNewsFeed(int userId, int tweetId) {
            PriorityQueue<int[]> newsFeeds = userIdNewsFeedsMap.get(userId);
            if (newsFeeds == null) {
                newsFeeds = new PriorityQueue<>((n1, n2) -> n2[1] - n1[1]);
                userIdNewsFeedsMap.put(userId, newsFeeds);
            }
            newsFeeds.offer(new int[]{tweetId, time});
            limit(newsFeeds);// drop old new
        }

        public List<Integer> getNewsFeed(int userId) {
            PriorityQueue<int[]> feeds = userIdNewsFeedsMap.get(userId);
            List<Integer> res = new ArrayList<>();
            if(feeds == null) {
                return res;
            }
            int i = 1;
            PriorityQueue<int[]> tmp = new PriorityQueue<>(feeds);
            while (!tmp.isEmpty() && i < 10) {
                res.add(tmp.poll()[0]);
                i++;
            }
            return res;
        }

        public void follow(int followerId, int followeeId) {
            // build relationship
            List<Integer> follows = userIdFollowsMap.get(followeeId);
            if (follows == null) {
                follows = new ArrayList<>();
                userIdFollowsMap.put(followeeId, follows);
            }
            if (!follows.contains(followerId)) {
                follows.add(followerId);
            } else {
                return;
            }

            // add followee's tweetIds
            LinkedList<int[]> followeeFeeds = ownerNewsFeedsMap.get(followeeId);
            PriorityQueue<int[]> myFeeds = userIdNewsFeedsMap.get(followerId);
            if (followeeFeeds != null) {
                if (myFeeds == null) {
                    myFeeds = new PriorityQueue<>((n1, n2) -> n2[1] - n1[1]);
                    userIdNewsFeedsMap.put(followerId, myFeeds);
                }
                for (int[] feed : followeeFeeds) {
                    myFeeds.offer(feed);
                }

                limit(myFeeds);
            }
        }

        private void limit(PriorityQueue<int[]> myFeeds) {
            if (myFeeds.size() > limit) {
                PriorityQueue<int[]> t = new PriorityQueue<>(myFeeds);
                myFeeds.clear();
                while (myFeeds.size() < limit) {
                    myFeeds.offer(t.poll());
                }
            }
        }

        public void unfollow(int followerId, int followeeId) {
            List<Integer> follows = userIdFollowsMap.get(followeeId);
            if (follows != null) {
                // remove relationship
                follows.remove((Object) followerId);

                // remove followee's tweetIds
                LinkedList<int[]> followeeFeeds = ownerNewsFeedsMap.get(followeeId);
                PriorityQueue<int[]> myFeeds = userIdNewsFeedsMap.get(followerId);
                if (followeeFeeds != null && myFeeds != null) {
                    Set<Integer> tweetIdSet = followeeFeeds.stream().map(f -> f[0]).collect(Collectors.toSet());
                    myFeeds.removeIf(i -> tweetIdSet.contains(i[0]));
                }
            }
        }
    }

    @Test
    public void case1() {
        Twitter twitter = new Twitter();
        twitter.postTweet(1, 5); // User 1 posts a new tweet (id = 5).
        twitter.getNewsFeed(1);  // User 1's news feed should return a list with 1 tweet id -> [5]. return [5]
        twitter.follow(1, 2);    // User 1 follows user 2.
        twitter.postTweet(2, 6); // User 2 posts a new tweet (id = 6).
        twitter.getNewsFeed(1);  // User 1's news feed should return a list with 2 tweet ids -> [6, 5]. Tweet id 6 should precede tweet id 5 because it is posted after tweet id 5.
        twitter.unfollow(1, 2);  // User 1 unfollows user 2.
        twitter.getNewsFeed(1);  // User 1's news feed should return a list with 1 tweet id -> [5], since user 1 is no longer following user 2.
    }

    @Test
    public void case2() {
        Twitter twitter = new Twitter();
        // ["Twitter","postTweet","getNewsFeed","follow","getNewsFeed","unfollow","getNewsFeed"]
        // [[],[1,1],[1],[2,1],[2],[2,1],[2]]
        twitter.postTweet(1, 1);
        System.out.println(twitter.getNewsFeed(1));
        twitter.follow(2, 1);
        System.out.println(twitter.getNewsFeed(2));
        twitter.unfollow(2, 1);
        System.out.println(twitter.getNewsFeed(2));

    }

    /**
     * ["Twitter","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","postTweet","getNewsFeed","follow","getNewsFeed","unfollow","getNewsFeed"]
     * [[],[1,5],[2,3],[1,101],[2,13],[2,10],[1,2],[1,94],[2,505],[1,333],[2,22],[1,11],[1,205],[2,203],[1,201],[2,213],[1,200],[2,202],[1,204],[2,208],[2,233],[1,222],[2,211],
     * [1],[1,2],[1],[1,2],[1]]
     */
    @Test
    public void case3() {
        Twitter twitter = new Twitter();
        twitter.postTweet(1, 5);
        twitter.postTweet(2, 3);
        twitter.postTweet(1, 101);
        twitter.postTweet(2, 13);
        twitter.postTweet(2, 10);
        twitter.postTweet(1, 2);
        twitter.postTweet(1, 94);
        twitter.postTweet(2, 505);
        twitter.postTweet(1, 333);
        twitter.postTweet(2, 22);
        twitter.postTweet(1, 11);
        twitter.postTweet(1, 205);
        twitter.postTweet(2, 203);
        twitter.postTweet(1, 201);
        twitter.postTweet(2, 213);
        twitter.postTweet(1, 200);
        twitter.postTweet(2, 202);
        twitter.postTweet(1, 204);
        twitter.postTweet(2, 208);
        twitter.postTweet(2, 233);
        twitter.postTweet(1, 222);
        twitter.postTweet(2, 211);

        System.out.println(twitter.getNewsFeed(1));
        twitter.follow(1, 2);
        System.out.println(twitter.getNewsFeed(1));
        twitter.unfollow(1, 2);
        System.out.println(twitter.getNewsFeed(1));
        // [222,204,200,201,205,11,333,94,2,101],
        // [211,222,233,208,204,202,200,213,201,203],
        // [222,204,200,201,205,11,333,94,2,101]]

        // [222,204,200,201]

    }
}
