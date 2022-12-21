package org.xq.parser.envaluation;

import org.xq.parser.functions.IFunction;
import org.xq.parser.functions.FunctionFactory;

import java.util.HashMap;

public class EvaluationContext {
  public EvaluationContext parent;
  public HashMap<String, Object> values;
  private boolean caseSensitive = true;

  private EvaluationContext(EvaluationContext parent, boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
    this.parent = parent;
    this.values = new HashMap<>();
  }

  public EvaluationContext(boolean caseSensitive) {
    this(null, caseSensitive);
  }

  public EvaluationContext(EvaluationContext parent) {
    this(parent, parent.caseSensitive);
  }

  public EvaluationContext pushContext() {
    return new EvaluationContext(this);
  }

  public EvaluationContext popContext() {
    if (this.parent == null) {
      throw new RuntimeException("The parent is null.");
    }
    return this.parent;
  }

  public Object getVariable(String variableName) {
    String key = getKey(variableName);
    if (values.containsKey(key)) {
      return values.get(key);
    } else {
      return parent == null ? null : parent.getVariable(variableName);
    }
  }

  public boolean exist(String variableName) {
    String key = getKey(variableName);
    if (values.containsKey(key)) {
      return true;
    } else {
      return parent != null && parent.exist(variableName);
    }
  }

  public boolean hasValue(String variableName) {
    Object value = getVariable( variableName);
    return value != null;
  }

  public void setVariable(String variableName, Object value) {
    String key = getKey(variableName);
    if (values.containsKey(key)) {
      values.put(key, value);
    } else if (parent != null) {
      parent.setVariable(variableName, value);
    } else {
      throw new RuntimeException(String.format("The %s variable does not exist.", variableName));
    }
  }

  public void newVariable(String variableName, Object value) {
    if (hasValue(variableName)) {
      throw new RuntimeException(String.format("The %s variable exist.", variableName));
    }

    values.put(getKey(variableName), value);
  }

  public Object executeFunction(String name, Object... params) {
    IFunction function = FunctionFactory.getFunction(name);
    return function.evaluate(params);
  }

  public void levelUpVariable(String variableName) {
    String key = getKey(variableName);
    if (!values.containsKey(key)) {
      throw new RuntimeException(String.format("The %s variable does not exist in this scope.", variableName));
    }

    if (this.parent == null) {
      throw new RuntimeException("The parent context is null.");
    }

    if (this.parent.hasValue(variableName)) {
      throw new RuntimeException(String.format("The parent contains the %s variable.", variableName));
    }

    Object value = this.getVariable(key);
    this.values.remove(key);
    this.parent.newVariable(key, value);
  }

  private String getKey(String variableName) {
    String key = variableName;
    if (!this.caseSensitive) {
      key = variableName.toLowerCase();
    }

    return key;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (String key: this.values.keySet()) {
      Object value = getVariable(key);
      builder.append("Name: ").append(key).append(", Value: ").append(value.toString()).append("\r\n");
    }
    if (this.parent != null) {
      builder.append(parent.toString());
    }
    return builder.toString();
  }
}
