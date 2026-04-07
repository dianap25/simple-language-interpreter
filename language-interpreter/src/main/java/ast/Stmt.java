package ast;

import lexer.Position;

import java.util.List;

public sealed interface Stmt permits
        Stmt.Assignment,
        Stmt.If,
        Stmt.While,
        Stmt.Function,
        Stmt.Return,
        Stmt.Expression,
        Stmt.Block,
        Stmt.Sequence {

    Position position();

    record Assignment (
            String name,
            Expr value,
            Position position
    ) implements Stmt {}

    record If (
            Expr condition,
            Stmt thenStmt,
            Stmt elseStmt,
            Position position
    ) implements Stmt {}

    record While (
            Expr condition,
            Stmt body,
            Position position
    ) implements Stmt {}

    record Function (
            String name,
            List<String> parameters,
            Block body,
            Position position
    ) implements Stmt {}

    record Return (
            Expr value,
            Position position
    ) implements Stmt {}

    record Expression (
            Expr expression,
            Position position
    ) implements Stmt {}

    record Block (
            List<Stmt> statements,
            Position position
    ) implements Stmt {}

    record Sequence (
            List<Stmt> statements,
            Position position
    ) implements Stmt {}
}
