grammar WhereClause;

parse
 : expression EOF
 ;

expression
 : LPAREN expression RPAREN                       #parenExpression
 | left=IDENTIFIER op=comparator right=value      #comparatorExpression
 | left=expression op=binary right=expression     #binaryExpression
 ;

 value
 : DECIMAL                                        #decimalValue
 | TEXT                                           #textValue
 | DATE                                           #dateValue
 | TIME                                           #timeValue
 ;

comparator
 : GT | LT | EQ | NE
 ;

binary
 : AND | OR
 ;

AND        : 'AND' | 'and' ;
OR         : 'OR' | 'or' ;
GT         : 'gt' | 'GT' ;
LT         : 'lt' | 'LT' ;
EQ         : 'eq' | 'EQ' ;
NE         : 'ne' | 'NE' ;
LPAREN     : '(' ;
RPAREN     : ')' ;
DECIMAL    : '-'? [0-9]+ ( '.' [0-9]+ )? ;
IDENTIFIER : [a-zA-Z_] [a-zA-Z_0-9]* ;
TEXT       : '\'' [-]* [a-zA-Z_.,0-9 ]* '\'' ;
DATE       : '\'' DIGIT DIGIT DIGIT DIGIT '-' DIGIT DIGIT '-' DIGIT DIGIT '\'' ;
DIGIT      : [0-9] ;
TIME       : '\'' DIGIT DIGIT ':' DIGIT DIGIT ':' DIGIT DIGIT '\'' ;
WS         : [ \r\t\u000C\n]+ -> skip;