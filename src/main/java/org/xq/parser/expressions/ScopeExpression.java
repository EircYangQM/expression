package org.xq.parser.expressions;

import java.util.ArrayList;

public class ScopeExpression extends Expression implements IContainerExpression {
  private final boolean isTop;
  private ArrayList<Expression> expressions;

  public ScopeExpression(Boolean isTop) {
    expressions = new ArrayList<>();
    this.isTop = isTop;
  }

  public void addExpression(Expression expression) {
    expressions.add(expression);
  }

  public ArrayList<Expression> getExpressions() {
    return this.expressions;
  }

  public boolean isTop() {
    return isTop;
  }
}
