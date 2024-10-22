#include <stdio.h>
#include <stdlib.h>
#include <string.h>

short IsRowDuplicate(int *b,int len){
    for(int i=0;i<len;i++){
        for(int j=i+1;j<len;j++){
            if(b[i]==b[j]){
                return 1;
            }
        }
    }
    return 0;
}

short IsColDuplicate(int b[36][36],int len,int col){
    for(int i=0;i<len;i++){
        for(int j=i+1;j<len;j++){
            if(b[i][col]==b[j][col]){
                return 1;
            }
        }
    }
    return 0;
}

int main(int argc,char **argv){
    int i,j,N,A[6*6][6*6],B[6*6][6*6],res=0;
    memset(A,-1,36*36*sizeof(int));
    memset(B,-1,36*36*sizeof(int));

    FILE *stdIN;
    stdIN = fopen("dataIN5","r");
    fscanf(stdIN, "%d\n",&N);//获取数量
    for(i=0;i<N*N;i++){
        for(j=0;j<N*N;j++){
            fscanf(stdIN, "%d",&A[i][j]);
        }
    }
    for(i=0;i<N*N;i++){
        if(IsRowDuplicate(A[i],N*N)!=0){
            printf("row\n");
            res=1;
            break;
        }
    }
    for(i=0;i<N*N;i++){
        if(IsColDuplicate(A,N*N,i)!=0){
            printf("col\n");
            res=1;
            break;
        }
    }
    
    for(i=0;i<N*N;i++){
        for(j=0;j<N*N;j++){
            B[i/3*3+j/3][i%3*3+j%3]=A[i][j];
        }
    }
    for(i=0;i<N*N;i++){
        if(IsRowDuplicate(B[i],N*N)!=0){
            printf("sub\n");
            res=1;
            break;
        }
    }
    fclose(stdIN);
    if(res==1){
        printf("no\n");
    }else{
        printf("yes\n");
    }
    return 0;
}