package org.xq.expression.expressions;

import java.util.List;

public class IfExpression extends Expression implements IContainerExpression {
  private final List<Expression> conditions;
  private final List<Expression> scopes;
  private final Expression elseExpr;

  public IfExpression(List<Expression> conditions, List<Expression> scopes, Expression elseExpr) {
    this.conditions = conditions;
    this.scopes = scopes;
    this.elseExpr = elseExpr;
  }

  public List<Expression> getConditions() {
    return conditions;
  }

  public List<Expression> getScopes() {
    return scopes;
  }

  public Expression getElse() {
    return elseExpr;
  }
}
