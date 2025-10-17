//已知一课二叉树标准形式的存储结构,写一函数生成它的扩充标准形式的存储结构
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef int BElemType;
typedef struct BiTree{
    BElemType data;
    struct BiTree *lchild;
    struct BiTree *rchild;
} BiTree;

typedef struct BiTree2{
    BElemType data;
    struct BiTree2 *lchild,*rchild,*parent;
} BiTree2;

BiTree * CreateTree(BElemType a[],int len,int index)
{
    //从数组a中创建二叉树，len为数组a的长度-1。index初始值为0。
    if(index>=len) return NULL;
    BiTree *bt=malloc(sizeof(BiTree));
    bt->data=a[index];
    bt->lchild = CreateTree(a,len,2*index+1);
    bt->rchild = CreateTree(a,len,2*index+2);
    return bt;
}

void PreOrderTraverse(BiTree *t){
    if(t==NULL)return;
    printf("%d\t",t->data);
    PreOrderTraverse(t->lchild);
    PreOrderTraverse(t->rchild);
}
void InOrderTraverse(BiTree *t){
    if(t==NULL)return;
    InOrderTraverse(t->lchild);
    printf("%d\t",t->data);
    InOrderTraverse(t->rchild);
}
void PostOrderTraverse(BiTree *t){
    if(t==NULL)return;
    PostOrderTraverse(t->lchild);
    PostOrderTraverse(t->rchild);
    printf("%d\t",t->data);
}

BiTree2 * CreateTree2(BiTree *t1,BiTree2 *p){
    if(t1==NULL)return NULL;
    BiTree2 *t2;
    t2=malloc(sizeof(BiTree2));
    t2->data=t1->data;
    t2->parent=p;
    t2->lchild = CreateTree2(t1->lchild,t2);
    t2->rchild = CreateTree2(t1->rchild,t2);
    return t2;
}
void PreOrderTraverse2(BiTree2 *r){
    BiTree2 arr[15],*p;
    int i;
    i=-1;
    p=r;
    while (p!=NULL) {//走到左边尽头
        arr[++i]=*p;
        printf("%d\t",p->data);
        p=p->lchild;
    }
    while (i>-1) {
        p=&arr[i--];
        if(p->rchild!=NULL){
            p=p->rchild;
            while (p!=NULL) {//走到p左边尽头
                arr[++i]=*p;
                printf("%d\t",p->data);
                p=p->lchild;
            }
        }
    }
}
void InOrderTraverse2(BiTree2 *r){
    BiTree2 arr[15],*p;
    int i;
    i=-1;
    p=r;
    while (p!=NULL) {//走到root左边尽头
        arr[++i]=*p;
        p=p->lchild;
    }
    while (i>-1) {
        p=&arr[i--];
        printf("%d\t",p->data);
        if(p->rchild!=NULL){
            p=p->rchild;
            while (p!=NULL) {//走到p左边尽头
                arr[++i]=*p;
                p=p->lchild;
            }
        }
    }
}


int main() {
    BiTree *r1;
    BiTree2 *r2;
    BElemType arr[]={3,1,4,1,5,9,2,6,5,3,5,8,9,7,9},len;
    len=sizeof(arr)/sizeof(*arr);
    printf("数组二叉树标准形式的存储结构\n");
    r1 = CreateTree(arr,sizeof(arr)/sizeof(int),0);
    printf("先序\n");
    PreOrderTraverse(r1);
    printf("\n中序\n");
    InOrderTraverse(r1);
    printf("\n后序\n");
    PostOrderTraverse(r1);
    
    printf("\n生成它的扩充标准形式的存储结构\n");
    r2=malloc(sizeof(BiTree2));
    r2->data=r1->data;
    r2->lchild = CreateTree2(r1->lchild,r2);
    r2->rchild = CreateTree2(r1->rchild,r2);
    
    
    printf("\n非递归先序遍历\n");
    PreOrderTraverse2(r2);
    printf("\n非递归中序遍历\n");
    InOrderTraverse2(r2);
    
    printf("\n");
}
