#include <stdio.h>

void Merge(int *A,int n,int *B,int m){
    int curr_n=n,a1=0,a2,b1=0,i;
    while (b1<m) {
        //找到A[]中比当前数大的跳出
        while(A[a1]<=B[b1]&&a1<curr_n){
            a1++;
        }
        if(a1==curr_n){//a数组已近到最后一位
            for (; b1<m; b1++) {
                A[a1++]=B[b1];
            }
        }else{
            //往后平移一个位置
            for(a2=curr_n;a2>a1;a2--){
                A[a2]=A[a2-1];
            }
            A[a1]=B[b1];
            
            curr_n++;//a数组长度+1
            b1++;//b数组后移一位
        }
    }
}

int main()
{
    int a[20]={1,3,7,11,13,80,81,82},b[7]={2,4,6,12,20,21,22},i;
    Merge(a,8,b,7);
    for(i=0;i<15;i++){
        printf("%d ",a[i]);
    }
}
