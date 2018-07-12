#include <stdio.h>
#include <stdlib.h>

typedef struct Node{
    int data;
    struct Node *next;
} Link;

void RemoveDuplicates(Link *head){
    Link *p1,*p2,*p3,*p4;
    p1=head;
    while (p1!=NULL) {
        p3=p1;//p3指向p2前面一个
        p2=p1->next;
        while (p2!=NULL) {
            //删除一个节点
            if(p1->data==p2->data){
                p4=p2->next;//p4指向p2后面一个节点
                p3->next=p4;
                free(p2);
                printf("%d\n",p2->data);
                p2=p4;
            }
            p3=p2;
            p2=p2==NULL?NULL:p2->next;
        }
        p1=p1->next;
    }
}

void Print(Link *head){
    printf("link is : ");
    Link *p1;
    p1=head;
    while (p1!=NULL) {
        printf("%d",p1->data);
        p1=p1->next;
        if(p1!=NULL){
           printf("->");
        }
    }
    printf("\n");
}

Link* InitLink(int *A,int len){
    Link *cur = NULL,*next=NULL;
    for(int i=len-1;i>=0;i--){
        cur = malloc(sizeof(Link));
        if (NULL == cur){
            exit (1);
        }
        cur->data=A[i];
        cur->next=next;
        next=cur;
    }
    return cur;
}

int main()
{
    
    //Link link;
    int a[]={3,5,6,3,4,2,3,5,3};
    Link *head=InitLink(a,9);
    Print(head);
    RemoveDuplicates(head);
    Print(head);
}


