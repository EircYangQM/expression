package org.xq.parser.expressions;

public class AssignExpression extends Expression {
  private final Expression variable;
  private final Expression calculateExpression;

  public AssignExpression(Expression variable, Expression calculateExpression) {
    this.variable = variable;
    this.calculateExpression = calculateExpression;
  }

  public Expression getVariable() {
    return variable;
  }

  public Expression getCalculateExpression() {
    return calculateExpression;
  }
}
