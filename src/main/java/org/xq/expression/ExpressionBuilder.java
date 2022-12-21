package org.xq.expression;

import org.xq.expression.expressions.*;

import java.util.List;

public class ExpressionBuilder extends ExpressionVisitor<String> {
  private StringBuilder outputBuilder;
  private int indentLevel;

  public ExpressionBuilder() {
    outputBuilder = new StringBuilder();
  }

  public String build(Expression expression) {
    this.outputBuilder.setLength(0);
    this.indentLevel = 0;
    this.visit(expression);
    return this.outputBuilder.toString();
  }

  @Override
  public String visit(AssignExpression expression) {
    this.visit(expression.getVariable());
    this.outputBuilder.append(" = ");
    this.visit(expression.getCalculateExpression());
    return null;
  }

  @Override
  public String visit(DeclareExpression expression) {
    this.outputBuilder.append("let ");
    this.visit(expression.getExpression());
    return null;
  }

  @Override
  public String visit(VariableExpression expression) {
    this.outputBuilder.append(expression.getVariable());
    return null;
  }

  @Override
  public String visit(IfExpression expression) {
    List<Expression> conditions = expression.getConditions();
    List<Expression> scopes = expression.getScopes();
    Expression elseExpr = expression.getElse();
    int count = conditions.size();
    for (int i = 0; i < count; i++) {
      if (i == 0) {
        this.append("if ");
      } else {
        this.append(" else if ");
      }
      this.outputBuilder.append("(");
      this.visit(conditions.get(i));
      this.outputBuilder.append(")");
      this.visit(scopes.get(i));
    }

    if (elseExpr != null) {
      this.append("else ");
      this.visit(elseExpr);
    }
    return null;
  }

  @Override
  public String visit(WhileExpression expression) {
    this.append("while ");
    this.outputBuilder.append("(");
    this.visit(expression.getCondition());
    this.outputBuilder.append(")");
    this.visit(expression.getScope());
    return null;
  }

  @Override
  public String visit(AccessorExpression expression) {
    this.visit(expression.getVariable());
    if (expression.isIndexed()) {
      this.outputBuilder.append("[");
    } else {
      this.outputBuilder.append(".");
    }
    for (int i = 0; i < expression.getParams().size(); i++) {
      if (i != 0) {
        this.outputBuilder.append(",");
      }
      Expression subExpression = expression.getParams().get(i);
      this.visit(subExpression);
    }

    if (expression.isIndexed()) {
      this.outputBuilder.append("]");
    }
    return null;
  }

  @Override
  public String visit(OperatorExpression expression) {
    this.outputBuilder.append(expression.getOp());
    return null;
  }

  @Override
  public String visit(ValueExpression expression) {
    List<Expression> expressions = expression.getExpressionStack();
    if (expression.isQuoted()) {
      this.outputBuilder.append("(");
    }
    for (int i = 0; i < expressions.size(); i++) {
      if (i != 0) {
        this.outputBuilder.append(" ");
      }
      Expression subExpression = expression.getExpressionStack().get(i);
      this.visit(subExpression);
    }
    if (expression.isQuoted()) {
      this.outputBuilder.append(")");
    }
    return null;
  }

  @Override
  public String visit(FunctionExpression expression) {
    this.outputBuilder.append(expression.getName());
    this.outputBuilder.append("(");
    for (int i = 0; i < expression.getParams().size(); i++) {
      if (i != 0) {
        this.outputBuilder.append(", ");
      }
      Expression param = expression.getParams().get(i);
      this.visit(param);
    }
    this.outputBuilder.append(")");
    return null;
  }

  @Override
  public String visit(ConstantExpression expression) {
    if (expression.getType() == ConstantExpression.TYPE_STRING) {
      this.outputBuilder.append("\"").append(expression.getConstant()).append("\"");
    } else {
      this.outputBuilder.append(expression.getConstant());
    }
    return null;
  }

  @Override
  public String visit(ScopeExpression expression) {
    if (!expression.isTop()) {
      this.append("{");
      this.outputBuilder.append("\r\n");
      this.indentLevel++;
    }

    List<Expression> subExpressions = expression.getExpressions();
    int count = subExpressions.size();
    for (int i = 0; i < count; i++) {
      if (i != 0) {
        this.appendEnd();
      }
      Expression subExpression = subExpressions.get(i);
      this.append("");
      this.visit(subExpression);
      if (!(subExpression instanceof IContainerExpression)) {
        this.outputBuilder.append(";");
      }
    }

    if (!expression.isTop()) {
      this.indentLevel--;
      this.appendEnd();
      this.append("}");
    }
    return null;
  }

  private String printIndent(int indentLevel) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < indentLevel; i++) {
      builder.append("  ");
    }
    return builder.toString();
  }

  private void append(String output) {
    this.outputBuilder.append(printIndent(this.indentLevel)).append(output);
  }

  private void appendEnd() {
    this.outputBuilder.append("\r\n");
  }
}
