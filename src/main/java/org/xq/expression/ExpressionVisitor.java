package org.xq.expression;

import org.xq.expression.expressions.*;

public abstract class ExpressionVisitor<T> {
  public T visit(Expression expression) {
    if (expression instanceof AccessorExpression) {
      return this.visit((AccessorExpression)expression);
    } else if (expression instanceof AssignExpression) {
      return this.visit((AssignExpression)expression);
    } else if (expression instanceof ConstantExpression) {
      return this.visit((ConstantExpression)expression);
    } else if (expression instanceof DeclareExpression) {
      return this.visit((DeclareExpression)expression);
    } else if (expression instanceof FunctionExpression) {
      return this.visit((FunctionExpression)expression);
    } else if (expression instanceof IfExpression) {
      return this.visit((IfExpression)expression);
    } else if (expression instanceof OperatorExpression) {
      return this.visit((OperatorExpression)expression);
    } else if (expression instanceof ScopeExpression) {
      return this.visit((ScopeExpression)expression);
    } else if (expression instanceof ValueExpression) {
      return this.visit((ValueExpression)expression);
    } else if (expression instanceof VariableExpression) {
      return this.visit((VariableExpression)expression);
    } else if (expression instanceof WhileExpression) {
      return this.visit((WhileExpression)expression);
    } else if (expression instanceof AccessorExpression) {
      return this.visit((AccessorExpression)expression);
    } else {
      throw new RuntimeException(String.format("Invalid Expression. %s", expression.getClass().getName()));
    }
  }

  public abstract T visit(AssignExpression expression);
  public abstract T visit(ConstantExpression expression);
  public abstract T visit(DeclareExpression expression);
  public abstract T visit(FunctionExpression expression);
  public abstract T visit(OperatorExpression expression);
  public abstract T visit(ScopeExpression expression);
  public abstract T visit(ValueExpression expression);
  public abstract T visit(VariableExpression expression);
  public abstract T visit(IfExpression expression);
  public abstract T visit(WhileExpression expression);
  public abstract T visit(AccessorExpression expression);
}
