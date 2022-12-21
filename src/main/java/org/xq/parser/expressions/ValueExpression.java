package org.xq.parser.expressions;

import java.util.List;

public class ValueExpression extends Expression {
  private final List<Expression> expressionStack;
  private final boolean quoted;

  public ValueExpression(List<Expression> expressionStack, boolean quoted) {
    this.expressionStack = expressionStack;
    this.quoted = quoted;
  }

  public ValueExpression normalize() {
    return null;
  }

  public List<Expression> getExpressionStack() {
    return expressionStack;
  }

  public boolean isQuoted() {
    return quoted;
  }
}
