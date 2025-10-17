#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main() {
    int v[16],i,j=0,s=0,t;
    for (i=0; i<16; i++) {
        v[i]=2*i+1;
    }
    for (i=0; i<=10; i++) {
        for (j=0; j<=i; j++) {
            if(j%2==0)
                t=v[j];
            else
                t=-v[j];
            s+=t;
            printf("v[%d]=%d\n",j,t);
        }
        printf("s=%d\n\n",s);
    }
    printf("i=%d,j=%d,s=%d\n",i,j,s);
}