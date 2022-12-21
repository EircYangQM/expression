package org.xq.expression.expressions;

public class WhileExpression extends Expression implements IContainerExpression {
  private final Expression condition;
  private final Expression scope;


  public WhileExpression(Expression condition, Expression scope) {
    this.condition = condition;
    this.scope = scope;
  }

  public Expression getCondition() {
    return condition;
  }

  public Expression getScope() {
    return scope;
  }
}
