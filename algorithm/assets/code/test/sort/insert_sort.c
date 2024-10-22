#include <stdio.h>
int main(int argc,char *argv[]){
    int a[]={8,7,3,1,10,4,9,2,7,5},n=10,i,j,t;
    for(i=1;i<n;i++){
        t=a[i];
        printf("%d\n",t);
        print(a,n);
        if(a[i]>=a[i-1])
            continue;
        for (j=i;j>0&&a[j-1]>t;j--)
            a[j]=a[j-1];
        a[j]=t;
        print(a,n);
       
    }
    return 0;
}