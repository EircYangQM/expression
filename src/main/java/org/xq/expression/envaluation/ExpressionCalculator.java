package org.xq.expression.envaluation;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

// 00 postfix 	              expr++ expr--
// 01 unary 	                ++expr --expr +expr -expr ~ !
// 02 multiplicative 	        * / %
// 03 additive 	              + -
// 04 shift 	                << >> >>>
// 05 relational             	< > <= >= instanceof
// 06 equality 	              == !=
// 07 bitwise AND 	          &
// 08 bitwise exclusive OR 	  ^
// 09 bitwise inclusive OR  	|
// 10 logical AND           	&&
// 11 logical OR             	||
// 12 ternary                	? :
// 13 assignment            	= += -= *= /= %= &= ^= |= <<= >>= >>>=

abstract class Calculator {
  protected Object[] parameters;
  protected int current;
  protected int parameterCount;
  protected String op;
  private int priority;
  protected static final int TYPE_BIT = 0;
//  protected static final int TYPE_TINY_INT = 1;
//  protected static final int TYPE_SMALL_INT = 2;
  protected static final int TYPE_INT = 3;
  protected static final int TYPE_LONG = 4;
  protected static final int TYPE_DECIMAL = 5;
  protected static final int TYPE_FLOAT = 6;
  protected static final int TYPE_DOUBLE = 7;
  protected static final int TYPE_DATETIME = 8;
  protected static final int TYPE_BOOLEAN = 9;
  protected static final int TYPE_OBJECT = 10;
  protected static final int TYPE_STRING = 11;

  protected Calculator(String op, int parameterCount, int priority) {
    this.parameterCount = parameterCount;
    this.parameters = new Object[parameterCount];
    this.current = 0;
    this.op = op;
    this.priority = priority;
  }

  public int getParameterCount() {
    return this.parameterCount;
  }

  public String getOperator() {
    return this.op;
  }

  public void push(Object value) {
    if (this.current < this.parameterCount) {
      this.parameters[this.parameterCount - this.current - 1] = value;
      this.current ++;
    } else {
      throw new RuntimeException(String.format("The operator %s's parameter count is reached. count: %d", this.op, this.parameterCount));
    }
  }

  public Object[] getParameters() {
    return this.parameters;
  }

  public abstract Object calculate();

  @Override
  public String toString() {
    if (this.parameterCount == 1) {
      return this.op + this.convertValue(0, TYPE_OBJECT);
    } else if (this.parameterCount == 2) {
      return this.convertValue(0, TYPE_OBJECT) + this.op + this.convertValue(1, TYPE_OBJECT);
    } else {
      return "";
    }
  }

  public int getPriority() {
    return this.priority;
  }

  public int getTypeCode(Object value) {
    if (value instanceof ReferenceValue) {
      return this.getTypeCode(((ReferenceValue)value).getValue());
    } else if (value instanceof Byte) {
      return TYPE_BIT;
    } else if (value instanceof Integer) {
      return TYPE_INT;
    } else if (value instanceof Long) {
      return TYPE_LONG;
    } else if (value instanceof BigDecimal) {
      return TYPE_DECIMAL;
    } else if (value instanceof Float) {
      return TYPE_FLOAT;
    } else if (value instanceof Double) {
      return TYPE_DOUBLE;
    } else if (value instanceof Date) {
      return TYPE_DATETIME;
    } else if (value instanceof Boolean) {
      return TYPE_BOOLEAN;
    } else if (value instanceof String) {
      return TYPE_STRING;
    } else {
      return TYPE_OBJECT;
    }
  }

  public int getReturnType() {
    int maxType = -1;
    for (int i = 0; i < this.parameters.length; i++) {
      Object param = this.parameters[i];
      int currentType = getTypeCode(param);
      if (i == 0) {
        maxType = currentType;
      } else {
        if (currentType > maxType) {
          if (currentType > TYPE_DOUBLE) {
            maxType = TYPE_STRING;
          } else {
            maxType = currentType;
          }
        }
      }
    }

    return maxType;
  }

  protected <T> T convertValue(int index, int typeCode) {
    Object value = this.parameters[index];
    if (value instanceof ReferenceValue) {
      ReferenceValue referenceValue = (ReferenceValue) value;
      value = referenceValue.getValue();
    }
    switch (typeCode) {

//      case TYPE_TINY_INT: return ;
//      case TYPE_SMALL_INT: return ;
      case TYPE_INT: {
        if (value instanceof Integer) {
          return (T) value;
        } else if (value instanceof Byte) {
          return (T) (Integer)((Byte)value).intValue();
        } else {
          return (T)value;
        }
      }
      case TYPE_LONG: {
        if (value instanceof Long) {
          return (T) value;
        } else if (value instanceof Integer) {
          return (T) (Long) ((Integer)value).longValue();
        } else if (value instanceof Byte) {
          return (T) (Long) ((Byte)value).longValue();
        } else {
          return (T)value;
        }
      }
      case TYPE_DECIMAL: {
        if (value instanceof BigDecimal) {
          return (T) value;
        } else if (value instanceof Long) {
          return (T) new BigDecimal((Long) value);
        } else if (value instanceof Integer) {
          return (T) new BigDecimal((Integer)value);
        } else if (value instanceof Byte) {
          return (T) new BigDecimal((Byte)value);
        } else {
          return (T) value;
        }
      }
      case TYPE_FLOAT: {
        if (value instanceof Float) {
          return (T) value;
        } else if (value instanceof BigDecimal) {
          return (T) (Float) ((BigDecimal)value).floatValue();
        } else if (value instanceof Integer) {
          return (T) (Float) ((Integer)value).floatValue();
        } else if (value instanceof Byte) {
          return (T) (Float) ((Byte)value).floatValue();
        } else {
          return (T)value;
        }
      }
      case TYPE_DOUBLE: {
        if (value instanceof Double) {
          return (T) value;
        } else if (value instanceof Float) {
          return (T) (Double)((Float) value).doubleValue();
        } else if (value instanceof BigDecimal) {
          return (T) (Double) ((BigDecimal)value).doubleValue();
        } else if (value instanceof Integer) {
          return (T) (Double) ((Integer)value).doubleValue();
        } else if (value instanceof Byte) {
          return (T) (Double) ((Byte)value).doubleValue();
        } else {
          return (T)value;
        }
      }
      case TYPE_DATETIME: {
        if (value instanceof Date) {
          return (T) value;
        } else {
          throw new RuntimeException("Invalid value: " + value);
        }
      }
      case TYPE_BOOLEAN: {
        if (value instanceof Boolean) {
          return (T) value;
        } else {
          throw new RuntimeException("Invalid value: " + value);
        }
      }
      case TYPE_STRING : {
        return (T)value.toString();
      }
      case TYPE_OBJECT :
      case TYPE_BIT:
      default:
        return (T) value;
    }
  }

  protected String getTypeName(int typeCode) {
    switch (typeCode) {
      case TYPE_BIT: return "BIT";
      case TYPE_INT: return "INT";
      case TYPE_LONG: return "LONG";
      case TYPE_DECIMAL: return "DECIMAL";
      case TYPE_FLOAT: return "FLOAT";
      case TYPE_DOUBLE: return "DOUBLE";
      case TYPE_DATETIME: return "DATETIME";
      case TYPE_BOOLEAN: return "BOOLEAN";
      case TYPE_OBJECT : return "OBJECT";
      case TYPE_STRING : return "STRING";
      default: return "";
    }
  }
}

class Increase extends Calculator {
  private boolean opFirst;

  protected Increase(boolean opFirst) {
    super("++", 1, opFirst ? 2 : 1);
    this.opFirst = opFirst;
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    Object value;
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      default:             throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:       value = (Integer) convertValue(0, TYPE_INT)   + 1; break;
      case TYPE_LONG:      value = (Long) convertValue(0, TYPE_LONG)     + 1; break;
      case TYPE_FLOAT:     value = (Float) convertValue(0, TYPE_FLOAT)   + 1; break;
      case TYPE_DOUBLE:    value = (Double) convertValue(0, TYPE_DOUBLE) + 1; break;
      case TYPE_DECIMAL:   value = ((BigDecimal) convertValue(0, TYPE_DECIMAL)).add(new BigDecimal(1)); break;
    }

    Object parameter = this.getParameters()[0];
    Object retValue = convertValue(0, TYPE_OBJECT);
    if (parameter instanceof ReferenceValue) {
      ((ReferenceValue)parameter).assign(value);
    } else {
      throw new RuntimeException(String.format("The ++ operator only accept variable"));
    }

    return this.opFirst ? value : retValue;
  }

  @Override
  public String toString() {
    return opFirst ? "++" + this.getParameters()[0] : this.getParameters()[0] + "++";
  }
}

class Decrease extends Calculator {
  private boolean opFirst;

  protected Decrease(boolean opFirst) {
    super("--", 1, opFirst ? 2 : 1);
    this.opFirst = opFirst;
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    Object value;
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      default:             throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:       value = (Integer) convertValue(0, TYPE_INT)   - 1; break;
      case TYPE_LONG:      value = (Long) convertValue(0, TYPE_LONG)     - 1; break;
      case TYPE_FLOAT:     value = (Float) convertValue(0, TYPE_FLOAT)   - 1; break;
      case TYPE_DOUBLE:    value = (Double) convertValue(0, TYPE_DOUBLE) - 1; break;
      case TYPE_DECIMAL:   value = ((BigDecimal) convertValue(0, TYPE_DECIMAL)).subtract(new BigDecimal(1)); break;
    }

    Object parameter = this.getParameters()[0];
    Object retValue = convertValue(0, TYPE_OBJECT);
    if (parameter instanceof ReferenceValue) {
      ((ReferenceValue)parameter).assign(value);
    } else {
      throw new RuntimeException(String.format("The -- operator only accept variable"));
    }

    return this.opFirst ? value : retValue;
  }

  @Override
  public String toString() {
    return "--" + this.getParameters()[0];
  }
}

class NOT extends Calculator {
  protected NOT() {
    super("!", 1, 2);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_INT:
      case TYPE_LONG:
      case TYPE_STRING:
      default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_BOOLEAN: return !(Boolean) convertValue(0, TYPE_BOOLEAN);
    }
  }

  @Override
  public String toString() {
    return "!" + this.getParameters()[0];
  }
}

class Subtract extends Calculator {
  protected Subtract(boolean isSingle) {
    super("-", isSingle ? 1 : 2, isSingle ? 2 : 3);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    if (this.getParameterCount() == 1) {
      switch (type) {
        case TYPE_BIT:
        case TYPE_DATETIME:
        case TYPE_BOOLEAN:
        case TYPE_OBJECT:
        case TYPE_STRING:
        default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
        case TYPE_INT:     return - (Integer) convertValue(0, TYPE_INT);
        case TYPE_LONG:    return - (Long) convertValue(0, TYPE_LONG);
        case TYPE_FLOAT:   return - (Float) convertValue(0, TYPE_FLOAT);
        case TYPE_DOUBLE:  return - (Double) convertValue(0, TYPE_DOUBLE);
        case TYPE_DECIMAL: return (new BigDecimal(0)).subtract((BigDecimal) convertValue(0, TYPE_DECIMAL));
      }
    } else {
      switch (type) {
        case TYPE_BIT:
        case TYPE_DATETIME:
        case TYPE_BOOLEAN:
        case TYPE_OBJECT:
        case TYPE_STRING:
        default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
        case TYPE_INT:     return (Integer) convertValue(0, TYPE_INT)   - (Integer) convertValue(1, TYPE_INT);
        case TYPE_LONG:    return (Long) convertValue(0, TYPE_LONG)     - (Long) convertValue(1, TYPE_LONG);
        case TYPE_FLOAT:   return (Float) convertValue(0, TYPE_FLOAT)   - (Float) convertValue(1, TYPE_FLOAT);
        case TYPE_DOUBLE:  return (Double) convertValue(0, TYPE_DOUBLE) - (Double) convertValue(1, TYPE_DOUBLE);
        case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).subtract((BigDecimal) convertValue(1, TYPE_DECIMAL));
      }
    }
  }

  @Override
  public String toString() {
    if (this.getParameterCount() == 1) {
      return "-" + this.getParameters()[0];
    } else {
      return this.getParameters()[0] + "-" + this.getParameters()[1];
    }
  }
}

class Plus extends Calculator {
  protected Plus(boolean isSingle) {
    super("+", isSingle ? 1 : 2, isSingle ? 2 : 3);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    if (this.getParameterCount() == 1) {
      switch (type) {
        case TYPE_BIT:
        case TYPE_DATETIME:
        case TYPE_BOOLEAN:
        case TYPE_OBJECT:
        default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
        case TYPE_INT:     return convertValue(0, TYPE_INT);
        case TYPE_LONG:    return convertValue(0, TYPE_LONG);
        case TYPE_FLOAT:   return convertValue(0, TYPE_FLOAT);
        case TYPE_DOUBLE:  return convertValue(0, TYPE_DOUBLE);
        case TYPE_STRING:  return convertValue(0, TYPE_STRING);
        case TYPE_DECIMAL: return convertValue(0, TYPE_DECIMAL);
      }
    } else {
      switch (type) {
        case TYPE_BIT:
        case TYPE_DATETIME:
        case TYPE_BOOLEAN:
        case TYPE_OBJECT:
        default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
        case TYPE_INT:     return (Integer) convertValue(0, TYPE_INT)   + (Integer) convertValue(1, TYPE_INT);
        case TYPE_LONG:    return (Long) convertValue(0, TYPE_LONG)     + (Long) convertValue(1, TYPE_LONG);
        case TYPE_FLOAT:   return (Float) convertValue(0, TYPE_FLOAT)   + (Float) convertValue(1, TYPE_FLOAT);
        case TYPE_DOUBLE:  return (Double) convertValue(0, TYPE_DOUBLE) + (Double) convertValue(1, TYPE_DOUBLE);
        case TYPE_STRING:  return convertValue(0, TYPE_STRING)          + (String) convertValue(1, TYPE_STRING);
        case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).add((BigDecimal) convertValue(1, TYPE_DECIMAL));
      }
    }
  }

  @Override
  public String toString() {
    if (this.getParameterCount() == 1) {
      return "+" + this.getParameters()[0];
    } else {
      return this.getParameters()[0] + "+" + this.getParameters()[1];
    }
  }
}

class Multiply extends Calculator {
  protected Multiply() {
    super("*", 2, 2);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT) * (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG) * (Long) convertValue(1, TYPE_LONG);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).multiply((BigDecimal) convertValue(1, TYPE_DECIMAL));
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT) * (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) * (Double) convertValue(1, TYPE_DOUBLE);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "*" + this.getParameters()[1];
  }
}

class Divide extends Calculator {
  protected Divide() {
    super("*", 2, 2);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT) / (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG) / (Long) convertValue(1, TYPE_LONG);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).divide((BigDecimal) convertValue(1, TYPE_DECIMAL));
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT) / (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) / (Double) convertValue(1, TYPE_DOUBLE);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "/" + this.getParameters()[1];
  }
}

class MOD extends Calculator {
  protected MOD() {
    super("%", 2, 2);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT) % (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG) % (Long) convertValue(1, TYPE_LONG);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).remainder((BigDecimal) convertValue(1, TYPE_DECIMAL));
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT) % (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) % (Double) convertValue(1, TYPE_DOUBLE);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "%" + this.getParameters()[1];
  }
}

class LeftShift extends Calculator {
  protected LeftShift() {
    super("<<", 2, 2);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      case TYPE_DECIMAL:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT) << (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG) << (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "<<" + this.getParameters()[1];
  }
}

class RightShift extends Calculator {
  protected RightShift() {
    super(">>", 2, 4);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      case TYPE_DECIMAL:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT) >> (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG) >> (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + ">>" + this.getParameters()[1];
  }
}

class UnsignRightShift extends Calculator {
  protected UnsignRightShift() {
    super(">>>", 2, 4);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_OBJECT:
      case TYPE_STRING:
      case TYPE_DECIMAL:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      default: throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:  return (Integer) convertValue(0, TYPE_INT) >>> (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)   >>> (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + ">>>" + this.getParameters()[1];
  }
}

class Greater extends Calculator {
  protected Greater() {
    super(">", 2, 5);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT)      > (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)       > (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT)    > (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) > (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) > 0;
      case TYPE_BIT: return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                         > 0;
      case TYPE_DATETIME: return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))          > 0;
      case TYPE_BOOLEAN: return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       > 0;
      case TYPE_STRING: return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))            > 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + ">" + this.getParameters()[1];
  }
}

class GreaterAndEqual extends Calculator {
  protected GreaterAndEqual() {
    super(">=", 2, 5);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT)      >= (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)       >= (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT)    >= (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) >= (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) >= 0;
      case TYPE_BIT: return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                         >= 0;
      case TYPE_DATETIME: return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))          >= 0;
      case TYPE_BOOLEAN: return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       >= 0;
      case TYPE_STRING: return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))            >= 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + ">=" + this.getParameters()[1];
  }
}

class Less extends Calculator {
  protected Less() {
    super("<", 2, 5);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT)      < (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)       < (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT)    < (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) < (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) < 0;
      case TYPE_BIT: return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                         < 0;
      case TYPE_DATETIME: return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))          < 0;
      case TYPE_BOOLEAN: return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       < 0;
      case TYPE_STRING: return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))            < 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "<" + this.getParameters()[1];
  }
}

class LessAndEqual extends Calculator {
  protected LessAndEqual() {
    super("<=", 2, 5);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT)      <= (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)       <= (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT)    <= (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) <= (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) <= 0;
      case TYPE_BIT: return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                         <= 0;
      case TYPE_DATETIME: return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))          <= 0;
      case TYPE_BOOLEAN: return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       <= 0;
      case TYPE_STRING: return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))            <= 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "<=" + this.getParameters()[1];
  }
}

class Equal extends Calculator {
  protected Equal() {
    super("==", 2, 6);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT: return (Integer) convertValue(0, TYPE_INT)      == (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG: return (Long) convertValue(0, TYPE_LONG)       == (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT: return (Float) convertValue(0, TYPE_FLOAT)    == (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE: return (Double) convertValue(0, TYPE_DOUBLE) == (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL: return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) == 0;
      case TYPE_BIT: return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                         == 0;
      case TYPE_DATETIME: return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))          == 0;
      case TYPE_BOOLEAN: return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       == 0;
      case TYPE_STRING: return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))            == 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "==" + this.getParameters()[1];
  }
}

class NotEqual extends Calculator {
  protected NotEqual() {
    super("!=", 2, 6);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      default:              throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:        return (Integer) convertValue(0, TYPE_INT)      != (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG:       return (Long) convertValue(0, TYPE_LONG)        != (Long) convertValue(1, TYPE_LONG);
      case TYPE_FLOAT:      return (Float) convertValue(0, TYPE_FLOAT)      != (Float) convertValue(1, TYPE_FLOAT);
      case TYPE_DOUBLE:     return (Double) convertValue(0, TYPE_DOUBLE)    != (Double) convertValue(1, TYPE_DOUBLE);
      case TYPE_DECIMAL:    return ((BigDecimal) convertValue(0, TYPE_DECIMAL)).compareTo((BigDecimal) convertValue(1, TYPE_DECIMAL)) != 0;
      case TYPE_BIT:        return ((Byte) convertValue(0, TYPE_BIT)).compareTo((Byte) convertValue(1, TYPE_BIT))                     != 0;
      case TYPE_DATETIME:   return ((Date) convertValue(0, TYPE_DATETIME)).compareTo((Date) convertValue(1, TYPE_DATETIME))           != 0;
      case TYPE_BOOLEAN:    return ((Boolean) convertValue(0, TYPE_BOOLEAN)).compareTo((Boolean) convertValue(1, TYPE_BOOLEAN))       != 0;
      case TYPE_STRING:     return ((String) convertValue(0, TYPE_STRING)).compareTo((String) convertValue(1, TYPE_STRING))           != 0;
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "!=" + this.getParameters()[1];
  }
}

class BitwiseAND extends Calculator {
  protected BitwiseAND() {
    super("&", 2, 7);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:      return (Integer) convertValue(0, TYPE_INT)      & (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG:     return (Long) convertValue(0, TYPE_LONG)        & (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "&" + this.getParameters()[1];
  }
}

class BitwiseInclusiveOR extends Calculator {
  protected BitwiseInclusiveOR() {
    super("|", 2, 9);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:      return (Integer) convertValue(0, TYPE_INT)      | (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG:     return (Long) convertValue(0, TYPE_LONG)        | (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "|" + this.getParameters()[1];
  }
}

class BitwiseExclusiveOR extends Calculator {
  protected BitwiseExclusiveOR() {
    super("^", 2, 8);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_BOOLEAN:
      case TYPE_STRING:
      default:
        throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_INT:      return (Integer) convertValue(0, TYPE_INT)      ^ (Integer) convertValue(1, TYPE_INT);
      case TYPE_LONG:     return (Long) convertValue(0, TYPE_LONG)        ^ (Long) convertValue(1, TYPE_LONG);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "^" + this.getParameters()[1];
  }
}

class AND extends Calculator {
  protected AND() {
    super("&&", 2, 10);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_INT:
      case TYPE_LONG:
      case TYPE_STRING:
      default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_BOOLEAN: return (Boolean) convertValue(0, TYPE_BOOLEAN) && (Boolean) convertValue(1, TYPE_BOOLEAN);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "&&" + this.getParameters()[1];
  }
}

class OR extends Calculator {
  protected OR() {
    super("||", 2, 10);
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_INT:
      case TYPE_LONG:
      case TYPE_STRING:
      default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_BOOLEAN: return (Boolean) convertValue(0, TYPE_BOOLEAN) || (Boolean) convertValue(1, TYPE_BOOLEAN);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "||" + this.getParameters()[1];
  }
}

class Ternary extends Calculator {
  private boolean isSecond;

  protected Ternary(boolean second) {
    super("?", 3, 12);
    this.isSecond = second;
  }

  @Override
  public Object calculate() {
    int type = getReturnType();
    boolean value;
    switch (type) {
      case TYPE_OBJECT:
      case TYPE_FLOAT:
      case TYPE_DOUBLE:
      case TYPE_DECIMAL:
      case TYPE_BIT:
      case TYPE_DATETIME:
      case TYPE_INT:
      case TYPE_LONG:
      case TYPE_STRING:
      default:           throw new RuntimeException(String.format("%s is not support in %s.", this.getOperator(), getTypeName(type)));
      case TYPE_BOOLEAN: value = (Boolean) convertValue(0, TYPE_BOOLEAN); break;
    }

    if (value) {
      return convertValue(1, TYPE_OBJECT);
    } else {
      return convertValue(2, TYPE_OBJECT);
    }
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "?" + this.getParameters()[1] + ":" + this.getParameters()[2];
  }
}

class Assign extends Calculator {
  protected Assign() {
    super("=", 2, 13);
  }

  @Override
  public Object calculate() {
    if (!(this.parameters[0] instanceof ReferenceValue)) {
      throw new RuntimeException(String.format("The %s should be a variable.", this.parameters[0]));
    }

    Object value = this.convertValue(1, TYPE_OBJECT);
    ((ReferenceValue)this.parameters[0]).assign(value);
    return value;
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + "=" + this.getParameters()[1];
  }
}

class AssignWithCalc extends Calculator {
  private Calculator calculator;

  protected AssignWithCalc(String op, Calculator calculator) {
    super(op, calculator.parameterCount, 13);
    this.calculator = calculator;
  }

  @Override
  public Object calculate() {
    if (!(this.parameters[0] instanceof ReferenceValue)) {
      throw new RuntimeException(String.format("The %s should be a variable.", this.parameters[0]));
    }

    for (int i = 0; i < this.parameterCount; i++) {
      this.calculator.push(this.parameters[i]);
    }

    Object value = this.calculator.calculate();
    ((ReferenceValue)this.parameters[0]).assign(value);
    return value;
  }

  @Override
  public String toString() {
    return this.getParameters()[0] + this.op + this.getParameters()[1];
  }
}

class CalculatorBuilder {
  public Calculator build(String operator) {
    return build(operator, false);
  }

  public Calculator build(String operator, boolean singleOp) {
    switch (operator) {
      case "-"    : return new Subtract(singleOp);
      case "+"    : return new Plus(singleOp);
      case "*"    : return new Multiply();
      case "/"    : return new Divide();
      case "%"    : return new MOD();
      case ">>"   : return new RightShift();
      case ">>>"   : return new UnsignRightShift();
      case "<<"   : return new LeftShift();
      case ">"    : return new Greater();
      case ">="   : return new GreaterAndEqual();
      case "<"    : return new Less();
      case "<="   : return new LessAndEqual();
      case "=="   : return new Equal();
      case "!="   : return new NotEqual();
      case "&"    : return new BitwiseAND();
      case "|"    : return new BitwiseInclusiveOR();
      case "^"    : return new BitwiseExclusiveOR();
      case "&&"   : return new AND();
      case "||"   : return new OR();
      case "++"   : return new Increase(singleOp);
      case "--"   : return new Decrease(singleOp);
      case "!"    : return new NOT();
      case "?"    : return new Ternary(false);
      case ":"    : return new Ternary(true);
      case "="    : return new Assign();
      case "+="   : return new AssignWithCalc("+=", new Plus(false));
      case "-="   : return new AssignWithCalc("-=", new Subtract(false));
      case "*="   : return new AssignWithCalc("*=", new Multiply());
      case "/="   : return new AssignWithCalc("/=", new Divide());
      case "%="   : return new AssignWithCalc("%=", new MOD());
      case "&="   : return new AssignWithCalc("&=", new BitwiseAND());
      case "|="   : return new AssignWithCalc("|=", new BitwiseInclusiveOR());
      case "<<="  : return new AssignWithCalc("<<=", new LeftShift());
      case ">>="  : return new AssignWithCalc(">>=", new RightShift());
      case ">>>=" : return new AssignWithCalc(">>>=", new UnsignRightShift());
      default: throw new RuntimeException(String.format("The %s is not supported.", operator));
    }
  }
}

class ExpressionExecutor {
  private static final int P_TYPE_OP = 1;
  private static final int P_TYPE_VALUE = 2;

  private Stack<Calculator> operators;
  private Stack<Object> parameters;
  private Calculator current;
  private CalculatorBuilder calculatorBuilder;
  private int lastType;

  public ExpressionExecutor() {
    this.operators = new Stack<>();
    this.parameters = new Stack<>();
    this.calculatorBuilder = new CalculatorBuilder();
    this.current = null;
    this.lastType = -1;
  }

  public void pushOperator(String operator) {
    Calculator inserted;
    if (this.lastType == -1 || this.lastType == P_TYPE_OP) {
      inserted = calculatorBuilder.build(operator, true);
    } else {
      inserted = calculatorBuilder.build(operator);
    }
    this.lastType = P_TYPE_OP;

    if (current == null) {
      current = inserted;
      return;
    }

    while (current.getPriority() <= inserted.getPriority()) {
      if (current.parameterCount == 3 && current.parameterCount == inserted.parameterCount && operator.equals(":")) {
        // Do nothing and return.
        return;
      }

      this.calculateInternal();
      if (current == null) {
        break;
      }
    }

    if (current != null) {
      this.operators.add(current);
    }

    this.current = inserted;
  }

  public void pushValue(Object value) {
    this.parameters.add(value);
    this.lastType = P_TYPE_VALUE;
  }

  public Object calculate() {
    if (this.current == null && !this.operators.empty()) {
      this.current = this.operators.pop();
    }

    while (this.current != null) {
      this.calculateInternal();
    }

    if (this.parameters.empty()) {
      return null;
    } else {
      return this.parameters.pop();
    }
  }

  public void reset() {
    this.parameters.clear();
    this.operators.clear();
    this.lastType = -1;
    this.current = null;
  }

  private void calculateInternal() {
    for (int i = 0; i < this.current.getParameterCount(); i++) {
      Object value = this.parameters.pop();
      if (value == null) {
        throw new RuntimeException(String.format("The calculator need %d parameters, but it only %d", this.current.getParameterCount(), i ));
      }
      this.current.push(value);
    }

    this.parameters.add(this.current.calculate());
    if (this.operators.empty()) {
      this.current = null;
    } else {
      this.current = this.operators.pop();
    }
  }
}

class ReferenceValue {
  public ReferenceValue parent;
  public String name;
  public boolean isIndexed;
  public Object[] params;
  public EvaluationContext context;
  public boolean isNew;

  public ReferenceValue(EvaluationContext context) {
    this.context = context;
  }

  public void assign(Object value) {
    if (name == null) {
      if (this.parent == null) {
        throw new RuntimeException("The reference value parent is null.");
      }

      Object instance = this.parent.getValue();
      Object param = this.params[0];
      if (this.isIndexed) {
        if (!(param instanceof Integer)) {
          throw new RuntimeException("The reference index should be int.");
        }

        Array.set(instance, (Integer) this.params[0], value);
      } else {
        if (!(param instanceof String)) {
          throw new RuntimeException("The reference index should be int.");
        }

        try {
          instance.getClass().getField((String)this.params[0]).set(instance, value);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      if (this.isNew) {
        this.context.newVariable(this.name, value);
      } else {
        this.context.setVariable(this.name, value);
      }
    }
  }

  public Object getValue() {
    if (name == null) {
      if (this.parent == null) {
        throw new RuntimeException("The reference value parent is null.");
      }

      Object instance = this.parent.getValue();
      Object param = this.params[0];
      if (this.isIndexed) {
        if (!(param instanceof Integer)) {
          throw new RuntimeException("The reference index should be int.");
        }

        return Array.get(instance, (Integer) this.params[0]);
      } else {
        if (!(param instanceof String)) {
          throw new RuntimeException("The reference index should be int.");
        }

        try {
          return instance.getClass().getField((String)this.params[0]).get(instance);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      return this.context.getVariable(this.name);
    }
  }
}
