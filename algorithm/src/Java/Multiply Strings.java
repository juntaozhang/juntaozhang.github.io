M

想法不难。turn into int[], 然后每个位子乘积，然后余数carrier移位。

但是做起来有很多坑。适合面试黑。    

1. 数字‘123’， 在数组里面， index == 0 是 ‘1’。 但是我们平时习惯从最小位数开始乘积，就是末尾的'3'开始。
	所以！翻转两个数字先！我去。这个是个大坑。

2. 乘积product，和移动Carrier都很普通。

3. ！！最后不能忘了再翻转。

4. 最后一个看坑。要是乘积是0，就返回‘0’。 但是这个其实可以在开头catch到没必要做到结尾catch。

用到几个StringBuffer的好东西:   
reverse（）；    
sb.deleteCharAt(i)   

找数字，或者26个字母，都可以：    
s.charAt(i) - '0'; //数字    
s.charAt(i) - 'a'; //字母   

```
/*
Given two numbers represented as strings, return multiplication of the numbers as a string.

Note: The numbers can be arbitrarily large and are non-negative.

Hide Company Tags Facebook
Hide Tags Math String
Hide Similar Problems (M) Add Two Numbers (E) Plus One (E) Add Binary

*/
/*
    Thoughts:
    1. too long to multiply int. so convert to int[]
    2. Multiply by definition:
        a. create a product[] of num1.size() + num2.size() - 1
        b. catches each product[i + j]
    3. for loop on product array again, to carry over the carries

    if both null, return null.
    if both "", return ""
    
    O(m + n)
*/
public class Solution {
    public String multiply(String num1, String num2) {
        if (num1 == null || num2 == null) {
            return "";
        } else if (num1.length() == 0 || num2.length() == 0) {
            return num1.length() == 0 ? num2 : num1;
        } else if (num1.equals("0") || num2.equals("0")) {
            return "0";
        }
        //reverse string, so to calculate from 0 base. easier to calculate
        num1 = new StringBuffer(num1).reverse().toString();
        num2 = new StringBuffer(num2).reverse().toString();
     
        //product array. extra leading space for carriers
        //normally just need num1.length() + num2.length() -1
        int[] product = new int[num1.length() + num2.length()];
        
        //Calculate the product normally
        for (int i = 0; i < num1.length(); i++) {
        	int a = num1.charAt(i) - '0';
            for (int j = 0; j < num2.length(); j++) {
            	int b = num2.charAt(j) - '0';
                product[i + j] += a * b;
            }
        }
        
        //calcualte and output
        //remember, now the string is reversed calculated. 
        //so every time, add to index 0. so it will all reverse back; OR, append, and reverse later.
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < product.length; i++) {
           	int number = product[i] % 10;
           	int carrier = product[i] / 10;
           	sb.append(number);
            if (i < product.length - 1) {
                product[i + 1] += carrier;
            } 
        }
        sb.reverse();
        //trim leading 0's
        while (sb.length() > 0 && sb.charAt(0) == '0') {
        	sb.deleteCharAt(0);
        }

        return sb.toString();    
    }
}





```