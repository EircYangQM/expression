package org.xq.expression;

import java.sql.Types;

public class Variable<T> {
  public static final Variable<Object> NULL = new Variable<>(new Object(), Types.NULL, true);

  public final T value;
  public final int typeCode;
  public final boolean isNull;

  public Variable(T value, int typeCode, boolean isNull) {
    this.value = value;
    this.typeCode = typeCode;
    this.isNull = isNull;
  }

  @Override
  public String toString() {
    return "Value{" +
        "value=" + value +
        ", isNull=" + isNull +
        '}';
  }
}
