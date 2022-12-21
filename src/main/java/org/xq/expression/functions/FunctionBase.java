package org.xq.expression.functions;

import org.xq.expression.Variable;

public abstract class FunctionBase implements IFunction {

  protected <T> T assertParam(int index, String name, boolean required, Variable... params) {
    if(checkNull(index, name,  required,params)) {
      return null;
    }

    return (T)params[index].value;
  }

  private boolean checkNull(int index, String name, boolean required, Variable... params) {
    if (index >= params.length) {
      if (required) {
        throw new RuntimeException(String.format("The % parameter is required.", name));
      } else {
        return true;
      }
    }

    return false;
  }
}
