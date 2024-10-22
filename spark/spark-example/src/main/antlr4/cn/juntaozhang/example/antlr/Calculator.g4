grammar Calculator;

line: expr EOF;

expr:'(' expr ')'                   # parenExpr
    |   expr ('*'|'/') expr         # mulOrDiv
    |   expr ('+'|'-') expr         # addOrSub
    |   INT                         # int
    ;
WS:     [ \t\n\r]+ -> skip ;
INT:    [0-9]+ ;

