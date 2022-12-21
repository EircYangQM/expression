package org.xq.expression.expressions;

public class ConstantExpression extends Expression {
  public static final int TYPE_STRING = 1;
  public static final int TYPE_NUMBER = 2;
  public static final int TYPE_BOOL = 3;

  private final String constant;
  private final int type;

  public ConstantExpression(String constant, int type) {
    this.constant = constant;
    this.type = type;
  }

  public String getConstant() {
    return constant;
  }

  public int getType() {
    return type;
  }
}
