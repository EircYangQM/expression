package org.xq.parser.expressions;

public class OperatorExpression extends Expression {
  private final String op;

  public OperatorExpression(String op) {
    this.op = op;
  }

  public String getOp() {
    return op;
  }
}
