# KV Cache
Cache K and V: Reduce the number of KV calculations repeated each time. 

Why is Q not cached? 
Because the old Q vector did not participate in subsequent calculations.

[大模型推理 Prefill 和 Decoder 阶段详解](https://www.bilibili.com/video/BV1nHqgYFEcq)：
prefill means init prompt to KV Cache

[KV cache原理](https://www.bilibili.com/video/BV17CPkeEEzk)：
```text
中华人-民-> 共, '民'上轮预测的token 还没有进入K V 的cache， '共' 是需要预测的token。

输入:  t_4 ("民")： 因为KV Cache所以不关心 t_1,t_2,t_3
        ↓
Embedding:  ℝ^1 → ℝ^64
        ↓
投影:   Q_4 = x·W_Q  → ℝ^64
        K_4 = x·W_K  → ℝ^64
        V_4 = x·W_V  → ℝ^64
        ↓
Cache:  K_all = [K_1..K_4]  4×64
        V_all = [V_1..V_4]  4×64
        ↓
QK:     Q_4(1×64) · K_all^T(64×4) = scores(1×4)
        ↓
Softmax → weights(1×4)
        ↓
QV:     weights(1×4) · V_all(4×64) = output(1×64)
        ↓
FFN → 投影到词表 → 预测 t_5
```

Cache 更新：把 K4,V4  写回 Cache，供下一步使用:
```text
KV Cache 里存着前 3 步的结果：
Cache_K = [K_1, K_2, K_3]   每个 K_i ∈ ℝ^64，整体形状 3 × 64
Cache_V = [V_1, V_2, V_3]   每个 V_i ∈ ℝ^64，整体形状 3 × 64
拼接当前步：
K_all = [K_1, K_2, K_3, K_4]  →  形状 4 × 64
V_all = [V_1, V_2, V_3, V_4]  →  形状 4 × 64
```