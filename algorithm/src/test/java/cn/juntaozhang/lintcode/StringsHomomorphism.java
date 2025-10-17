package cn.juntaozhang.lintcode;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class StringsHomomorphism {
  /**
   * @param s a string
   * @param t a string
   * @return true if the characters in s can be replaced to get t or false
   */
  public boolean isIsomorphic(String s, String t) {
    if (s == null && t == null) {
      return true;
    }
    if (s == null || t == null || s.length() != t.length()) {
      return false;
    }
    Map<Character, Character> map = new HashMap<>();
    Map<Character, Character> map2 = new HashMap<>();
    for (int i = 0; i < s.length(); i++) {
      if (map.get(s.charAt(i)) == null && map2.get(t.charAt(i)) == null) {
        map.put(s.charAt(i), t.charAt(i));
        map2.put(t.charAt(i), s.charAt(i));
      } else if (map.get(s.charAt(i)) == null ||
          map2.get(t.charAt(i)) == null ||
          (map.get(s.charAt(i)) != t.charAt(i)) ||
          (map2.get(t.charAt(i)) != s.charAt(i))) {
        return false;
      }

    }
    return true;
  }

  public boolean isIsomorphic2(String s, String t) {
    // Write your code here
    int[] m1 = new int[128];
    int[] m2 = new int[128];
    for (int i = 0; i < s.length(); ++i) {
      int cs = (int) s.charAt(i);
      int ts = (int) t.charAt(i);
      if (m1[cs] == 0 && m2[ts] == 0) {
        m1[cs] = ts;
        m2[ts] = 1;
      } else if (m1[cs] != ts) {
        return false;
      }
    }
    return true;
  }


  @Test
  public void isIsomorphic() {
//    System.out.println(isIsomorphic("a`%ii,VEZQc_BSU%ObO5<sX81B/bOw+CNUd#Uav*P!Ax!#>hh,k#b/|>4ixFQZl+l!?bJjakbQbGglEb<g>Hf81m@A9GIvbd]qh?y__t+E(Iyv7zUEfZF{81VaM-0u?]tG=_fFR/XJ=X{-,oRpxE9u*VNYlM", "b`%ii-WE[Qc_BSV%OcO5<sX82B/cOw+CNVd#Vbv*P!Bx!#?hh-k#c/|?4ixFQ[l+l!?cJkbkcQcGhlEc<h?Hf82m@B9GIvcd]rh?y__t+E(Iyv7{VEf[F{82WbN/0u?]tG=_fFR/XJ=X{/-oRpxE9u*WNYlN"));
//    System.out.println(isIsomorphic("aab","bbc"));
//    System.out.println(isIsomorphic("egg", "ade"));
    System.out.println(isIsomorphic(null, null));
  }
}
