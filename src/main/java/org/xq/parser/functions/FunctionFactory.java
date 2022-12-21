package org.xq.parser.functions;

import java.util.HashMap;

public class FunctionFactory {
  public static HashMap<String, IFunction> FUNCTIONS = new HashMap<>();

  static {
//    FUNCTIONS.put("expose", new ExposeFunction());
  }

  public static IFunction getFunction(String name) {
    String key = name.toLowerCase();
    if(FUNCTIONS.containsKey(key)) {
      return FUNCTIONS.get(key);
    }

    throw new RuntimeException(String.format("The %s function does not found.", name));
  }
}
