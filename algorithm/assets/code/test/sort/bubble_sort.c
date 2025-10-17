//https://zh.wikipedia.org/wiki/%E5%86%92%E6%B3%A1%E6%8E%92%E5%BA%8F
//冒泡不断交换然后找出最大的,比较次数比较多
#include <stdio.h>
void print(int *p ,int n){
	int i;
	for(i=0;i<n;i++){
		printf("%d ", p[i]);
	}
	printf("\n");
}
void bubble_sort(int arr[], int len) {
	int i, j, temp;
	for (i = 0; i < len - 1; i++){
		for (j = 0; j < len - 1 - i; j++)
			if (arr[j] > arr[j + 1]) {
				temp = arr[j];
				arr[j] = arr[j + 1];
				arr[j + 1] = temp;
			}
		print(arr,len);
	}
}
int main(int argc,char **argv){
	int A[]={7,3,8,2,9},n=5,i,j,tmp;
	print(A,n);
	bubble_sort(A,n);
	return 0;
}