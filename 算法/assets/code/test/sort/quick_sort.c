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
void sort(int *A,int start,int end,int n){
    if(start>=end)return;
    int mid=A[end],left=start,right=end-1;
    printf("\nbefore mid=%d start=%d end=%d\n",mid,start,end);
    print(A,n);
    //找一个基准，按基准分成两部分
    while(left<right){
        while(A[left]<mid && left<right)left++;
        while(A[right]>mid && left<right)right--;
        if(right!=left)
            swap(&A[right],&A[left]);
    }
    if(A[left]>A[end]){
        swap(&A[end],&A[left]);
    }else{
        left++;
    }
    //分别对两部分排序
    printf("after\n");
    print(A,n);
    sort(A,start,left-1,n);
    sort(A,left,end,n);
}

void quick_sort_recursive(int arr[], int start, int end) {
    if (start >= end)
        return;//這是為了防止宣告堆疊陣列時當機
    int mid = arr[end];
    int left = start, right = end - 1;
    while (left < right) {
        while (arr[left] < mid && left < right)
            left++;
        while (arr[right] >= mid && left < right)
            right--;
        swap(&arr[left], &arr[right]);
    }
    if (arr[left] >= arr[end])
        swap(&arr[left], &arr[end]);
    else
        left++;
    printf("\nmid=%d start=%d end=%d\n",mid,start,end);
    print(arr,5);
    if (left) {
        quick_sort_recursive(arr, start, left - 1);
        quick_sort_recursive(arr, left + 1, end);
    } else {
        quick_sort_recursive(arr, left + 1, end);
    }
}
void quick_sort(int arr[], int len) {
    quick_sort_recursive(arr, 0, len - 1);
}
int main(int argc,char *argv[]){
    int A[]={7,3,8,2,9},n;
//    int A[]={10,8,12,7,5},n;
    n=sizeof(A)/sizeof(*A);
    printf("orignal\n");
    print(A,n);
//    sort(A,0,n-1,n);
    quick_sort(A,n);
    printf("final\n");
    print(A,n);
    return 0;
}