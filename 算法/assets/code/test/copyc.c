#include <stdio.h>
#include <stdlib.h>

int main(){
	FILE *in,*out;
	char c,tmp[10];
	in = fopen("123","r");
	printf("请输入copy文件名称：\n");
	scanf("%s",tmp);
	out = fopen(tmp,"w");
	while((c=fgetc(in))!=-1){
		fputc(c,out);
	}
	fclose(in);33
	fclose(out);
	return 0;
}