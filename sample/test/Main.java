package test;

import org.xq.parser.ExpressionParser;
import org.xq.parser.envaluation.EvaluationContext;
import org.xq.parser.envaluation.ExpressionEngine;
import org.xq.parser.expressions.Expression;

public class Main {
  public static void main(String[] args) {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 3; let c = 1; let b = a < 3 ? 2 + 3 : c > 1 ? 2 : 1; expose(b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    System.out.println("b value is " + context.getVariable("b"));
  }
}