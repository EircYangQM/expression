package org.xq.expression.expressions;

public class VariableExpression extends Expression {
  private final String variable;

  public VariableExpression(String variable) {
    this.variable = variable;
  }

  public String getVariable() {
    return variable;
  }
}
