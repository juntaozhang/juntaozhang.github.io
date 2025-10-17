#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(){
	char str[3][10],tmp[10],n=3,i,j=0;
	FILE *out;
	for(i=0;i<3;i++){
		scanf("%s",str[i]);
	}
	
	for(i=0;i<n;i++){
		for(j=i+1;j<n;j++){
			if(strcmp(str[i],str[j])>0){
				strcpy(tmp,str[i]);
				strcpy(str[i],str[j]);
				strcpy(str[j],tmp);
			}
		}
	}
	printf("===after===\n");
	out=fopen("sort_str.data","w");
	for(i=0;i<n;i++){
		printf("%s\n",str[i]);
		//fputs(str[i],out);
		//fputc('\n',out);
		fprintf(out, "%s\n", str[i]);
	}
	fclose(out);
	return 0;
}