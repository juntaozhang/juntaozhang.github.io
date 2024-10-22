#include <stdio.h>
#include <stdlib.h>

/*
printf("%[m][.n]s", str ); 
m   输出最小宽度，单位字节，若str长度不足m，前补空格
.n   仅输出字符串str的前n位
*/
int main(){
	FILE *f1;
	char c,tmp[10];
	f1=fopen("dataOUT","r+");//文件读写
	while((c=getchar())!='#'){
		fputc(c,f1);
	}
	fputc('\n',f1);
	fflush(f1);
	printf("read from file\n");
	rewind(f1);
	while((c=fgetc(f1))!=-1){
        printf("%c", c);
	}

	printf("read skip from file\n");
	rewind(f1);	
	fseek(f1,-12L,2);
	fread(tmp,4,10,f1);
    printf("%.10s\n",tmp);
	printf("end\n");
	fclose(f1);
	return 0;
}