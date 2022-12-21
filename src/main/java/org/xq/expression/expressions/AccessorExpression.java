package org.xq.expression.expressions;

import java.util.List;

public class AccessorExpression extends Expression {
  private final Expression variable;
  private final List<Expression> params;
  private final boolean indexed;

  public AccessorExpression(Expression variable, List<Expression> params, boolean indexed) {
    this.variable = variable;
    this.params = params;
    this.indexed = indexed;
  }

  public Expression getVariable() {
    return variable;
  }

  public List<Expression> getParams() {
    return params;
  }

  public boolean isIndexed() {
    return indexed;
  }
}
