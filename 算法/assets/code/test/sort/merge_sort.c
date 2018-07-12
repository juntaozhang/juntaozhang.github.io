#include <stdio.h>
void print(int *p ,int n){
    int i;
    for(i=0;i<n;i++){
        printf("%d ", p[i]);
    }
    printf("\n");
}
void swap(int *x, int *y) {
    int t = *x;
    *x = *y;
    *y = t;
}
void sort(int arr[],int tmp[],int start,int end){
    if(start>=end)return;
    int len=end-start,mid=(len>>1)+start,k;
    int start1=start,end1=mid;
    int start2=mid+1,end2=end;
    
    sort(arr,tmp,start1,end1);
    sort(arr,tmp,start2,end2);
    
    k=start;
    while(start1<=end1&&start2<=end2)
        tmp[k++]=arr[start1]>arr[start2]?arr[start2++]:arr[start1++];
    //剩余start1放入tmp
    while(start1<=end1)
        tmp[k++]=arr[start1++];
    while(start2<=end2)
        tmp[k++]=arr[start2++];
    printf("\nstart=%d,end=%d\n",start,end);
    print(arr,9);
    for(k=start;k<=end;k++)
        arr[k]=tmp[k];
    print(arr,9);
    
}

int main(int argc,char *argv[]){
    int arr[]={8,7,3,1,10,4,9,2,7,5};
    const int n=9;
    int tmp[n];
    sort(arr,tmp,0,n-1);
    return 0;
}