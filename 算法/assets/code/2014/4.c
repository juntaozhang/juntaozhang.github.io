#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int split(char *str,int len,char regex,char result[][1000]){
    int i,num=0,j;
    for (i=0,j=0; i<len; i++) {
        if((int)str[i]==regex){
            j=0;
            num++;
        }else{
            result[num][j++]=str[i];
        }
        
    }
    return num;
}
int main() {
    char arr[30][1000];
    int num,i;
    char str[]="Get Spark from the downloads page of the project website. This documentation is for Spark version 2.0.2. Spark uses Hadoop’s client libraries for HDFS and YARN. Downloads are pre-packaged for a handful of popular Hadoop versions. Users can also download a “Hadoop free” binary and run Spark with any Hadoop version by augmenting Spark’s classpath.    If you’d like to build Spark from source, visit Building Spark.Spark runs on both Windows and UNIX-like systems (e.g. Linux, Mac OS). It’s easy to run locally on one machine — all you need is to have java installed on your system PATH, or the JAVA_HOME environment variable pointing to a Java installation.Spark runs on Java 7+, Python 2.6+/3.4+ and R 3.1+. For the Scala API, Spark 2.0.2 uses Scala 2.11. You will need to use a compatible Scala version (2.11.x).";
    num = split(str,sizeof(str),',',arr);
    for (i=0; i<num; i++) {
        printf("num:%d=>%s\n",i,arr[i]);
    }
}
