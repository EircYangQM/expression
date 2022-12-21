package org.xq.expression.expressions;

public class DeclareExpression extends Expression {
  private final Expression expression;

  public DeclareExpression(Expression expression) {
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }
}
