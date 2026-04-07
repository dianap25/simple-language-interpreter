# Simple Language Interpreter (Java)

## Overview

This project implements a simple interpreter for a dynamically typed, strongly typed programming language.

The interpreter:

* reads source code from **stdin**
* executes the program
* prints final values of global variables to **stdout**

The language supports:

* variables
* arithmetic and logical expressions
* conditionals (`if`)
* loops (`while`)
* functions and recursion
* lists

---

## Data Types

Supported types:

* `int`
* `double`
* `bool`
* `string`
* `List`

### Notes

* The language is **dynamically typed** - type is inferred at first assignment.
* The language is **strongly typed** - type cannot change after assignment.

Example:

```txt
x = 5        // int
x = 10       // OK
x = "text"   // runtime error
```

---

## Variables

* Created via assignment
* Mutable
* Scope:

  * global
  * function-local

Example:

```txt
x = 10
```

---

## Operators

### Arithmetic

* `+`, `-`, `*`, `/`

### Logical

* `&&`, `||`, `!`

### Comparison

* `==`, `!=`, `<`, `>`, `<=`, `>=`

### Assignment

* `=`

### String

* `+` (concatenation)

---

## Type Conversions

Explicit conversions only.

### Allowed:

* `bool -> int`

  * `true -> 1`, `false -> 0`
* `int -> double`
* `double -> int` (truncation)
* `int/double/bool -> string`

Example:

```txt
x = int(5.9)     // 5
y = "value: " + string_int(5)
```

### Not allowed:

* `string -> int/double/bool`

---

## Control Flow

### If

```txt
if x > 10 then y = 1 else y = 0
```

### While

```txt
while x < 3 do x = x + 1
```

### Multiple statements

```txt
x = x + 1, y = y + 2
```

---

## Functions

### Definition

```txt
fun add(a, b) { return a + b }
```

### Call

```txt
x = add(2, 3)
```

### Features

* parameters passed by value
* recursion supported
* no overloading

---

## Lists

Ordered collection of values.

Example:

```txt
values = [1, 2, 3]
```

Minimal operations:

* get ()
* size ()

---

## Error Handling

The interpreter reports basic syntax, semantic, runtime errors:

* undefined variable
* invalid operation (e.g. `string - int`)
* division by zero
* wrong number of function arguments
* invalid condition type (non-bool in `if` / `while`)

Example:

```txt
x = "abc" - 2   // error
```

---

## Grammar (EBNF)

```ebnf
INT_LITERAL      =   DIGIT , {DIGIT};
DOUBLE_LITERAL   =   DIGIT , {DIGIT} , "." , DIGIT, {DIGIT};
STRING_LITERAL   =   “"” , {STRING_CHAR}, “"” ;
BOOL_LITERAL     =   "true" | "false"  ;
IDENTIFIER       =   LETTER, {LETTER | DIGIT | "_"};


PROGRAM       = { STATEMENT } ;

STATEMENT     = ASSIGNMENT
              | IF
              | WHILE
              | FUNCTION
              | RETURN
              | EXPR ;

ASSIGNMENT    = IDENT, "=", EXPRESSION ;

IF            = "if", EXPRESSION ,"then", STATEMENT, "else", STATEMENT ;

WHILE         = "while", EXPRESSION, "do", STATEMENT ;

FUNCTION      = "fun", IDENT, "(" , [PARAMS], ")" ,"{" ,{ STATEMENT } ,"}" ;

RETURN        = "return" , EXPRESSION ;

PARAMS        = IDENT,  { "," IDENT } ;

EXPRESSION    = LOGICAL_OR ;

LOGICAL_OR    = LOGICAL_AND { "||" LOGICAL_AND } ;
LOGICAL_AND   = EQUALITY { "&&" EQUALITY } ;
EQUALITY      = COMPARISON { ("==" | "!=") COMPARISON } ;
COMPARISON    = TERM { (">" | "<" | ">=" | "<=") TERM } ;
TERM          = FACTOR { ("+" | "-") FACTOR } ;
FACTOR        = UNARY { ("*" | "/") UNARY } ;
UNARY         = ("!" | "-") UNARY | PRIMARY ;

POSTFIX       = PRIMARY, {POSTFIXPART } ;
POSTFIXPART   = (".", IDENTIFIER)
                | ("(", [ARGS], ")");
PRIMARY       = INT_LITERAL
                | DOUBLE_LITERAL
                | STRING_LITERAL
                | BOOL_LITERAL
                | "null"
                | IDENTIFIER
                | LIST
                | "(" , EXPRESSION, ")";


ARGS          = EXPRESSION { "," EXPRESSION } ;

LIST          = "[" [EXPRESSION { "," EXPRESSION }] "]" ;
```

---

## Input / Output

### Input

Program is read from **stdin**

Example:

```bash
cat program.txt | java -jar interpreter.jar
```

### Output

Final values of global variables:

```txt
x: 2
y: 8
```

---

## Project Structure

* `lexer` – tokenization
* `parser` – AST construction
* `ast` – syntax tree
* `interpreter` – execution engine

Pipeline:

```
source → lexer → parser → AST → interpreter → result
```

---

