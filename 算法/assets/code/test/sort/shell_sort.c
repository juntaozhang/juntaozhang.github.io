#include <stdio.h>

void shell_sort(int *a,int len){
    int gap,i,tmp,j;
    for(gap=len>>1;gap>=1;gap=gap>>1)
        for(i=gap;i<len;i++){
            tmp=a[i];
            for(j=i-gap;j>=0&&a[j]>tmp;j-=gap)
                a[j+gap]=a[j];
            a[j+gap]=tmp;
        }
}
void print(int *p ,int n){
    int i;
    for(i=0;i<n;i++){
        printf("%d ", p[i]);
    }
    printf("\n");
}
int main(){
    int a[]={3,23,5,2,7,1,2,6,20,13},len;
    len=sizeof(a)/sizeof(*a);
    print(a,len);
    shell_sort(a,len);
    print(a,len);
    
}