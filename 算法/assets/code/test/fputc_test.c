#include <stdio.h>
#include <stdlib.h>

//键盘输入一些字符，逐个写入文件，“#”跳出
int main(){
	char str1[10],c;
	FILE *fp;
	printf("请输入文件名称：\n");
	scanf("%s",str1);
	if((fp=fopen(str1,"w"))==NULL){
		printf("创建文件失败\n");
		exit(1);
	}
	c = getchar();//后去 \n
	printf("输入#结束\n");
	while((c = getchar())!='#'){//getchar函数从标准输入里读取下一个字符，返回类型为int型，返回值为用户输入的ASCⅡ码，出错返回EOF。
		putchar(c);
		fputc(c,fp);
	}
	fclose(fp);

}