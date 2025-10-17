package cn.juntaozhang.lintcode;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.*;

/**
 * 
 */
public class MinimumSpanningTree {
    class Connection {
        public String city1, city2;
        public int cost;

        public Connection(String city1, String city2, int cost) {
            this.city1 = city1;
            this.city2 = city2;
            this.cost = cost;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Connection{");
            sb.append("city1='").append(city1).append('\'');
            sb.append(", city2='").append(city2).append('\'');
            sb.append(", cost=").append(cost);
            sb.append('}');
            return sb.toString();
        }
    }

    @Test
    public void lowestCost() {
        System.out.println(lowestCost1(
                Lists.newArrayList(
                        new Connection("A","B",1),
                        new Connection("B","C",2),
                        new Connection("A","C",2)
                )
        ));
//        System.out.println(lowestCost(
//                Lists.newArrayList(
//                        new Connection("0","1",7),
//                        new Connection("0","3",5),
//                        new Connection("1","2",8),
//                        new Connection("1","4",7),
//                        new Connection("2","4",5),
//                        new Connection("3","1",9),
//                        new Connection("3","4",15),
//                        new Connection("3","5",6),
//                        new Connection("4","5",8),
//                        new Connection("4","6",9),
//                        new Connection("5","6",11)
//                )
//        ));
    }

    public List<Connection> lowestCost1(List<Connection> connections) {
        List<Connection> res = new ArrayList<>();
        Collections.sort(connections, new Comparator<Connection>(){
            public int compare(Connection c1, Connection c2){
                if (c1.cost != c2.cost) {
                    return c1.cost - c2.cost;
                } else if (!c1.city1.equals(c2.city1)) {
                    return c1.city1.compareTo(c2.city1);
                } else {
                    return c1.city2.compareTo(c2.city2);
                }
            }
        });

        int[] graph = new int[connections.size() + 1];
        Map<String, Integer> map = new HashMap<>();
        int i = 0;
        for (Connection c : connections) {
            if (map.get(c.city1) == null) {
                map.put(c.city1, ++i);
            }
            if (map.get(c.city2) == null) {
                map.put(c.city2, ++i);
            }
        }

        for (Connection c : connections) {
            Integer i1 = map.get(c.city1);
            Integer i2 = map.get(c.city2);
            Integer j1 = find(i1, graph);
            Integer j2 = find(i2, graph);
            if (j1 != j2) {
                graph[j1] = j2;
                res.add(c);
            }
        }
        return res;
    }

//    private int find(int i, int[] graph) {
//        if (graph[i] == 0) {
//            return i;
//        }
//        return graph[i] = find(graph[i], graph);
//    }

    public List<Connection> lowestCost(List<Connection> connections) {

        // Write your code here
        Collections.sort(connections, comp);
        System.out.println(connections);
        Map<String, Integer> hash = new HashMap<String, Integer>();
        int n = 0;
        for (Connection connection : connections) {
            if (!hash.containsKey(connection.city1)) {
                hash.put(connection.city1, ++n);
            }
            if (!hash.containsKey(connection.city2)) {
                hash.put(connection.city2, ++n);
            }
        }

        int[] father = new int[n + 1];

        List<Connection> results = new ArrayList<Connection>();
        for (Connection connection : connections) {
            int num1 = hash.get(connection.city1);
            int num2 = hash.get(connection.city2);

            int root1 = find(num1, father);//连通性
            int root2 = find(num2, father);
            if (root1 != root2) {
                father[root1] = root2;
                results.add(connection);
            }
        }

        if (results.size() != n - 1)
            return new ArrayList<Connection>();
        return results;
    }

    static Comparator<Connection> comp = new Comparator<Connection>() {
        public int compare(Connection a, Connection b) {
            if (a.cost != b.cost)
                return a.cost - b.cost;
            if (a.city1.equals(b.city1)) {
                return a.city2.compareTo(b.city2);
            }
            return a.city1.compareTo(b.city1);
        }
    };

    public int find(int num, int[] father) {
        if (father[num] == 0)
            return num;

        return father[num] = find(father[num], father);//简化联通
    }

}
