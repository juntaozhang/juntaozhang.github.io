#include <stdio.h>
int main(){
    union{
        int a;
        char ch[2];
    } au;
    au.a=298;//00000001 00101010
    //2^5+2^3+2^1=32+8+2=42
    printf("%d %d\n",au.ch[0],au.ch[1]);//42 1
}
