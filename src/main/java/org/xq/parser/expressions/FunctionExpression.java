package org.xq.parser.expressions;

import java.util.List;

public class FunctionExpression extends Expression {
  private final String name;
  private final List<Expression> params;

  public FunctionExpression(String name, List<Expression> params) {
    this.name = name;
    this.params = params;
  }

  public String getName() {
    return name;
  }

  public List<Expression> getParams() {
    return params;
  }
}
