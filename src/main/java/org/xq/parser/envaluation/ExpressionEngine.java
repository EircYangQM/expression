package org.xq.parser.envaluation;

import org.xq.parser.ExpressionBuilder;
import org.xq.parser.ExpressionVisitor;
import org.xq.parser.Variable;
import org.xq.parser.expressions.*;
import org.xq.parser.functions.FunctionFactory;
import org.xq.parser.functions.IFunction;

import java.lang.reflect.Array;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ExpressionEngine extends ExpressionVisitor<Object> {
  private EvaluationContext root;
  private EvaluationContext current;
  private Stack<ExpressionExecutor> executors;
  private ExpressionBuilder builder;

  public EvaluationContext evaluate(EvaluationContext context, Expression expression) {
    this.executors = new Stack<>();
    this.builder = new ExpressionBuilder();
    this.root = context;
    this.current = context;
    this.visit(expression);
    return root;
  }

  @Override
  public Object visit(AssignExpression expression) {
    ReferenceValue referenceValue = (ReferenceValue) this.visit(expression.getVariable());
    Object value = this.visit(expression.getCalculateExpression());
    referenceValue.assign(value);
//    this.setVariable(referenceValue, value, false);
    return value;
  }

  @Override
  public Object visit(ConstantExpression expression) {
    if (expression.getType() == ConstantExpression.TYPE_STRING) {
      return new Variable<String>(expression.getConstant(), Types.VARCHAR, false);
    } else if (expression.getType() == ConstantExpression.TYPE_BOOL) {
      return ValueUtils.parseAsBool(expression.getConstant());
    } else if (expression.getType() == ConstantExpression.TYPE_NUMBER) {
      Integer integer = ValueUtils.parseAsInteger(expression.getConstant());
      if (integer != null) {
        return integer;
      }

      Long aLong = ValueUtils.parseAsLong(expression.getConstant());
      if (aLong != null) {
        return aLong;
      }

      Double aDouble = ValueUtils.parseAsDouble(expression.getConstant());
      if (aDouble != null) {
        return aDouble;
      }
    }

    throw new RuntimeException(String.format("Invalid Number value %s", expression.getConstant()));
  }

  @Override
  public Object visit(DeclareExpression expression) {
    ReferenceValue referenceValue;
    Object value = null;
    VariableExpression variableExpression = null;
    if (expression.getExpression() instanceof VariableExpression) {
      variableExpression = (VariableExpression)expression.getExpression();
    } else if (expression.getExpression() instanceof AssignExpression) {
      if (((AssignExpression)expression.getExpression()).getVariable() instanceof  VariableExpression) {
        variableExpression = (VariableExpression)((AssignExpression)expression.getExpression()).getVariable();
      } else {
        throw new RuntimeException("It is not variable statement. Statement: " + builder.build(((AssignExpression)expression.getExpression()).getVariable()));
      }
      value = this.visit(((AssignExpression)expression.getExpression()).getCalculateExpression());
    } else {
      throw new RuntimeException("Invalid declare statement. Statement: " + builder.build(expression));
    }

    referenceValue = (ReferenceValue) this.visit(variableExpression);
    referenceValue.isNew = true;
    referenceValue.assign(value);
    return value;
  }

  @Override
  public Object visit(FunctionExpression expression) {
    if ("expose".equalsIgnoreCase(expression.getName())) {
      for (Expression param: expression.getParams()) {
        if (!(param instanceof VariableExpression)) {
          throw new RuntimeException("Only Variable is available." + builder.build(param));
        }
        this.current.levelUpVariable(((VariableExpression)param).getVariable());
      }
      return null;
    } else {
      IFunction function = FunctionFactory.getFunction(expression.getName());
      List<Object> params = new ArrayList<>();
      for (Expression param: expression.getParams()) {
        params.add(this.visit(param));
      }
      return function.evaluate(params.toArray());
    }
  }

  @Override
  public Object visit(OperatorExpression expression) {
    this.executors.peek().pushOperator(expression.getOp());
    return null;
  }

  @Override
  public Object visit(ScopeExpression expression) {
    this.current = this.current.pushContext();
    for (Expression subExpression: expression.getExpressions()) {
      this.visit(subExpression);
    }
    this.current = this.current.popContext();
    return null;
  }

  @Override
  public Object visit(ValueExpression expression) {
    this.executors.push(new ExpressionExecutor());
    for (Expression subExpr: expression.getExpressionStack()) {
      if (subExpr instanceof OperatorExpression) {
        this.visit(subExpr);
      } else {
        this.executors.peek().pushValue(this.visit(subExpr));
      }
    }
    return this.executors.pop().calculate();
  }

  @Override
  public Object visit(VariableExpression expression) {
    ReferenceValue referenceValue = new ReferenceValue(this.current);
    referenceValue.name = expression.getVariable();
    return referenceValue;
  }

  @Override
  public Object visit(IfExpression expression) {
    this.current = this.current.pushContext();
    boolean isExecuted = false;
    for (int i = 0; i < expression.getConditions().size(); i++) {
      Object value = visit(expression.getConditions().get(i));
      if (value instanceof Boolean) {
        if ((Boolean)value) {
          this.visit(expression.getScopes().get(i));
          isExecuted = true;
          break;
        }
      } else {
        throw new RuntimeException("The If Expression should return bool for condition expression");
      }
    }

    if (!isExecuted && expression.getElse() != null) {
      this.visit(expression.getElse());
    }

    this.current = this.current.popContext();
    return null;
  }

  @Override
  public Object visit(WhileExpression expression) {
    while (true) {
      Object value = visit(expression.getCondition());
      if (value instanceof Boolean) {
        if ((Boolean)value) {
          this.visit(expression.getScope());
        } else {
          return null;
        }
      } else {
        throw new RuntimeException("The If Expression should return bool for condition expression");
      }
    }
  }

  @Override
  public Object visit(AccessorExpression expression) {
    ReferenceValue referenceValue = new ReferenceValue(this.current);
    Object parentValue = this.visit(expression.getVariable());;
    if (!(parentValue instanceof ReferenceValue)) {
      throw new RuntimeException(String.format("The assessor expression is incorrect. The parent is not reference value. parent: %s", this.builder.build(expression.getVariable())));
    }

    referenceValue.parent = (ReferenceValue)parentValue;
    referenceValue.isIndexed = expression.isIndexed();
    referenceValue.params = new Object[expression.getParams().size()];
    if (referenceValue.params.length == 0) {
      throw new RuntimeException(String.format("The accessor expression require one parameter at least."));
    }
    if (referenceValue.params.length != 0) {
      throw new RuntimeException(String.format("The accessor expression ONLY require one parameter."));
    }
    for (int i = 0; i < referenceValue.params.length; i++) {
      referenceValue.params[i] = this.visit(expression.getParams().get(i));
    }
    return referenceValue;
  }

  private void checkVariable(String variableName) {
    if (!this.current.hasValue(variableName)) {
      throw new RuntimeException(String.format("The required %s variable does not exist.", variableName));
    }
  }
}