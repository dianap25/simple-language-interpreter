package ast;

import lexer.Position;

import java.util.List;

public sealed interface Expr permits
        Expr.IntLiteral,
        Expr.DoubleLiteral,
        Expr.StringLiteral,
        Expr.BoolLiteral,
        Expr.Variable,
        Expr.Unary,
        Expr.Binary,
        Expr.Call,
        Expr.PropertyAccess,
        Expr.ListLiteral,
        Expr.Grouping {

    Position position();

    record IntLiteral(
            int value,
            Position position
    ) implements Expr {}

    record DoubleLiteral(
            double value,
            Position position
    ) implements Expr {}

    record StringLiteral(
            String value,
            Position position
    ) implements Expr {}

    record BoolLiteral(
            boolean value,
            Position position
    ) implements Expr {}

    record Variable(
            String name,
            Position position
    ) implements Expr {}

    record Unary(
            String operator,
            Expr right,
            Position position
    ) implements Expr {}

    record Binary(
            Expr left,
            String operator,
            Expr right,
            Position position
    ) implements Expr {}

    record Call(
            Expr callee,
            List<Expr> arguments,
            Position position
    ) implements Expr {}


    //e.g. list_name.size()
    record PropertyAccess(
            Expr target,
            String property,
            Position position
    ) implements Expr {}

    record ListLiteral(
            List<Expr> elements,
            Position position
    ) implements Expr {}

    // e.g. (2+4)
    record Grouping(
            Expr expression,
            Position position
    ) implements Expr {}
}
