//https://zh.wikipedia.org/wiki/%E9%80%89%E6%8B%A9%E6%8E%92%E5%BA%8F
//选择排序,只找出下标最大或最小的,找完一轮之后才交换,交换次数比较小
#include <stdio.h>
void print(int *p ,int n){
	int i;
	for(i=0;i<n;i++){
		printf("%d ", p[i]);
	}
	printf("\n");
}
void selection_sort(int arr[], int len) {
	int i, j, min, temp;
	for (i = 0; i < len - 1; i++) {
		min = i;
		for (j = i + 1; j < len; j++)
			if (arr[min] > arr[j])
				min = j;
	   	temp = arr[min];
		arr[min] = arr[i];
		arr[i] = temp;
		print(arr,len);
	}
}
int main(int argc,char **argv){
	int A[]={7,3,8,2,9},n=5,i,j,tmp;
	print(A,n);
	selection_sort(A,n);
	return 0;
}