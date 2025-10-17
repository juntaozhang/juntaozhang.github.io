# coding=utf-8
# Disjoint-set union 并查集 https://www.youtube.com/watch?v=gpmOaSBcbYA
class DSU(object):
    def __init__(self, n):
        self.parent = [i for i in range(n)]
        self.rank = [1] * n

    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]

    def union(self, x, y):
        xr, yr = self.find(x), self.find(y)
        if xr == yr:
            return
        if self.rank[xr] < self.rank[yr]:
            xr, yr = yr, xr
        self.parent[yr] = xr
        self.rank[xr] += self.rank[yr]


#
# class Solution:
#     def earliestAcq(self, logs: List[List[int]], N: int) -> int:
#         dsu = DSU(N)
#         logs.sort()
#         for log in logs:
#             a = log[1]
#             b = log[2]
#             dsu.union(a, b)
#             if dsu.sz[dsu.find(a)] == N:
#                 return log[0]
#         return -1
dsu = DSU(6)
dsu.union(0, 1)
dsu.union(3, 4)
dsu.union(2, 3)
dsu.union(1, 5)
dsu.union(2, 4)
dsu.union(0, 3)
print(dsu)
