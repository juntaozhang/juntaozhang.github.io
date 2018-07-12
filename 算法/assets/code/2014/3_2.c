#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int T(int n){
    int i,t0=0,t1=1,t2=1,s=0;
    if(n==0)return 0;
    else if(n<=2)return 1;
    for(i=3;i<=n;i++){
        s=t0+t1+t2;
        t0=t1;
        t1=t2;
        t2=s;
    }
    return s;
}
int main() {
    printf("T(0)=%d\n",T(0));
    printf("T(1)=%d\n",T(1));
    printf("T(2)=%d\n",T(2));
    printf("T(3)=%d\n",T(3));
    printf("T(4)=%d\n",T(4));
    printf("T(5)=%d\n",T(5));
}
