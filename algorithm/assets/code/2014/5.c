#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINE 10000
typedef struct Line{
    long hash;
    long num;
    long offset;
} Line;

void hash(char *s,Line *line,int offset){
    int sum,i;
    for (i=0;s[i]!='\n'; i++) {
        sum+=(int)s[i];
    }
    (*line).hash= sum%10007;
    (*line).num=i;
    (*line).offset=offset;
}

int compare(Line l1,Line l2,FILE *fin){
    char s1[MAX_LINE],s2[MAX_LINE];
    int i;
    //比较hash值 小到大
    if(l1.hash!=l2.hash)return (int)(l1.hash-l2.hash);
    //比较字符长度 大到小
    else if(l1.num!=l2.num) return (int)(l2.num+l1.num);
    //字典排序 大到小
    else{
        fseek(fin,l1.offset,SEEK_SET);
        fgets(s1,MAX_LINE,fin);
        fseek(fin,l2.offset,SEEK_SET);
        fgets(s2,MAX_LINE,fin);
        for(i=0;s1[i]!='\n';i++){
            if(s1[i]!=s2[i]){
                return (int)(s2[i]-(int)s1[i]);
            }
        }
        return 0;
    }
    
}

void shell_sort(Line lines[],const int len,FILE *fin){
    int gap, i, j;
    Line temp;
    for (gap = len >> 1; gap > 0; gap >>= 1)
        for (i = gap; i < len; i++) {
            temp = lines[i];
            for (j = i - gap; j >= 0 && compare(lines[j], temp, fin)>0; j -= gap)
                lines[j + gap] = lines[j];
            lines[j + gap] = temp;
        }

}

int main() {
    FILE *fin=fopen("data.in2", "r");
    FILE *fout=fopen("data.out", "w");
    char s[MAX_LINE];
    int n,i,offset=0;
    
    //获取n
    printf("please input n:\n");
    scanf("%d",&n);
    Line lines[n];
    int sorts[n];
//    Line lines = malloc(sizeof(Line)*n);

    
    //首先对文件全行求hash
    for (i=0; i<n; i++) {
        fgets(s,MAX_LINE,fin);
        hash(s,&lines[i],offset);
        offset+=lines[i].num+1; //加上最后'\n'
    }
    //根据hash排序,采用希尔排序n(lg(n))
    shell_sort(lines, n,fin);
    for (i=0; i<n; i++) {
        fseek(fin,lines[i].offset,SEEK_SET);
        fgets(s,MAX_LINE,fin);
        printf("%ld,%ld,%ld,%s\n",lines[i].hash,lines[i].num,lines[i].offset,s);
        fputs(s,fout);
    }
    
//    free(lines);
    fclose(fout);
    fclose(fin);
  
}
