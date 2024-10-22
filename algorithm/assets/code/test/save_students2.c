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
    Student stus[3];
    FILE *fp;
    int len=sizeof(Student);
    for(i=0;i<n;i++){
        scanf("%d%s%d%s",&stus[i].num,stus[i].name,&stus[i].age,stus[i].address);
    }
    fp=fopen("students2.data","w");
    fwrite(stus,len,3,fp);
    fclose(fp);
    fp=NULL;
    
    
    fp=fopen("students2.data","r");
    fread(stus,len,3,fp);
    fclose(fp);
    fp=NULL;
    for(i=0;i<n;i++){
        printf("%d,%s,%d,%s\n",stus[i].num,stus[i].name,stus[i].age,stus[i].address);
    }
    return 0;
}
