package cn.juntaozhang.leetcode.test1;

import org.junit.Test;

import java.util.*;

public class L355_Twitter1 {
    final static int LIMIT = 10;

    static class Feed implements Comparable<Feed> {
        int tweetId;
        int time;
        int userId;

        public Feed(int tweetId, int time, int userId) {
            this.tweetId = tweetId;
            this.time = time;
            this.userId = userId;
        }

        @Override
        public int compareTo(Feed f) {
            return this.time - f.time;
        }
    }

    static class User {
        final int userId;
        final List<User> followees; // who I follow
        final List<User> followers; // who follow me
        final PriorityQueue<Feed> myFeeds; // old feed to new feed

        public User(int userId) {
            this.userId = userId;
            this.followees = new ArrayList<>();
            this.followers = new ArrayList<>();
            this.myFeeds = new PriorityQueue<>();
        }

        public void postTweet(Feed feed) {
            myFeeds.offer(feed);
            if (myFeeds.size() > LIMIT) {
                myFeeds.poll();// pop old
            }
        }

        public List<Integer> getNewsFeed() {
            List<Integer> res = new ArrayList<>();
            // from new feed to old feed
            PriorityQueue<Feed> queue = new PriorityQueue<>((f1, f2) -> f2.time - f1.time);

            // my + followeeIds
            queue.addAll(myFeeds);
            for (User u : followees) {
                queue.addAll(u.myFeeds);
            }

            while (res.size() < LIMIT &&  !queue.isEmpty()) {
                res.add(Objects.requireNonNull(queue.poll()).tweetId);
            }

            return res;
        }

        public void follow(User followee) {
            if(followee == null) return;
            if(followees.contains(followee)) return;
            followees.add(followee);
            followee.followers.add(this);
        }

        public void unfollow(User followee) {
            if(followee == null) return;
            if(!followees.contains(followee)) return;
            followees.remove(followee);
            followee.followers.remove(this);
        }
    }

    static class Twitter {
        final Map<Integer, User> userIdMap;
        int time;
        public Twitter() {
            this.time = 0;
            this.userIdMap = new HashMap<>();
        }

        public void postTweet(int userId, int tweetId) {
            Feed feed = new Feed(tweetId, time++, userId);
            User u = userIdMap.get(userId);
            if(u == null) {
                u = new User(userId);
                userIdMap.put(userId, u);
            }
            u.postTweet(feed);
        }

        public List<Integer> getNewsFeed(int userId) {
            User u = userIdMap.get(userId);
            if(u == null) {
                return new ArrayList<>();
            }
            return u.getNewsFeed();
        }

        public void follow(int followerId, int followeeId) {
            User follower = userIdMap.computeIfAbsent(followerId, k -> new User(followerId));
            User followee = userIdMap.computeIfAbsent(followeeId, k -> new User(followerId));
            follower.follow(followee);
        }

        public void unfollow(int followerId, int followeeId) {
            User follower = userIdMap.get(followerId);
            if(follower == null) return;
            User followee = userIdMap.get(followeeId);
            follower.unfollow(followee);
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
