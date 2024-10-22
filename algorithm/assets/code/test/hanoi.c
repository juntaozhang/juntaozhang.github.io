#include <stdio.h>
//汉诺塔
void move(char x,int n,char z){
	printf("move %d from %c to %c\n",n,x,z);
}

void hanoi(int n,char x,char y,char z){
	
	if(n==1){
		move(x,1,z);
	}else{
		hanoi(n-1,x,z,y);
		move(x,n,z);
		hanoi(n-1,y,x,z);
	}
}

int main(){
	printf("\nthis is %d hanoi:\n", 1);
	hanoi(1,'x','y','z');
	printf("\nthis is %d hanoi:\n", 2);
	hanoi(2,'x','y','z');
	printf("\nthis is %d hanoi:\n", 3);
	hanoi(3,'x','y','z');
	printf("\nthis is %d hanoi:\n", 4);
	hanoi(4,'x','y','z');
	return 0;
}