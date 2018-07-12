#include <stdio.h>
#include <stdlib.h>
//练习使用fwrite fread
typedef struct 
{
	int num;
	char name[10];
	int age;
	char address[30];
} Student;


int main(){
	int i,n=3;
	Student *stu;
	FILE *fp;
	int len=sizeof(Student);
	stu = malloc(len);
	fp=fopen("students.data","w");
	for(i=0;i<n;i++){
		scanf("%d%s%d%s",&stu->num,stu->name,&stu->age,stu->address);
		fwrite(stu,len,1,fp);

	}
	fclose(fp);
	fp=NULL;
	fp=fopen("students.data","r");
	for(i=0;i<n;i++){
		fread(stu,len,1,fp);
		printf("%d,%s,%d,%s\n",stu->num,stu->name,stu->age,stu->address);
	}
	free(stu);
	stu=NULL;
	fclose(fp);
	fp=NULL;
	return 0;
}