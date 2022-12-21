package expression;

import junit.framework.TestCase;
import org.xq.expression.*;
import org.xq.expression.envaluation.EvaluationContext;
import org.xq.expression.envaluation.ExpressionEngine;
import org.xq.expression.expressions.Expression;

public class ParserTest extends TestCase {
  public void test_parser01() {
    Expression ex = new ExpressionParser().parse("d = fdasfd * Dfads;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("d = fdasfd * Dfads;", builder.build(ex));
  }

  public void test_parser02() {
    Expression ex = new ExpressionParser().parse("let a = b * c;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * c;", builder.build(ex));
  }

  public void test_parser03() {
    Expression ex = new ExpressionParser().parse("let a = b * (c + d);");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * (c + d);", builder.build(ex));
  }

  public void test_parser04() {
    Expression ex = new ExpressionParser().parse("let a = b * Main(c + d);");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * Main(c + d);", builder.build(ex));
  }

  public void test_parser05() {
    Expression ex = new ExpressionParser().parse("let a = b * Main(c + d); m = s;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * Main(c + d);\r\nm = s;", builder.build(ex));
  }

  public void test_parser06() {
    Expression ex = new ExpressionParser().parse("let a = b * Main(c + d); if (m) {m = s;}");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * Main(c + d);\r\nif (m){\r\n  m = s;\r\n}", builder.build(ex));
  }

  public void test_parser07() {
    Expression ex = new ExpressionParser().parse("let a = b * Main(c + d); while (m) {m = s;}");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * Main(c + d);\r\nwhile (m){\r\n  m = s;\r\n}", builder.build(ex));
  }

  public void test_parser08() {
    Expression ex = new ExpressionParser().parse("let a = b * Main(c + d); if (m) {m = s;} else if (cm =d) m = d;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * Main(c + d);\r\nif (m){\r\n  m = s;\r\n} else if (cm = d)m = d;", builder.build(ex));
  }

  public void test_parser09() {
    Expression ex = new ExpressionParser().parse("let a = b * c.M;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b * c.M;", builder.build(ex));
  }

  public void test_parser10() {
    Expression ex = new ExpressionParser().parse("let a = b ++;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = b ++;", builder.build(ex));
  }

  public void test_parser11() {
    Expression ex = new ExpressionParser().parse("let a = ++ b;");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = ++ b;", builder.build(ex));
  }

  public void test_parser12() {
    Expression ex = new ExpressionParser().parse("let a = 1; let b = 5; expose(a, b);");
    ExpressionBuilder builder = new ExpressionBuilder();
    assertEquals("let a = 1;\r\nlet b = 5;\r\nexpose(a, b);", builder.build(ex));
  }

  public void test_evaluation01() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1; let b = 5; expose(a, b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(1, context.getVariable("a"));
    assertEquals(5, context.getVariable("b"));
  }

  public void test_evaluation02() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1 + 4; let b = 5; b = a + b; expose(a, b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(5, context.getVariable("a"));
    assertEquals(10, context.getVariable("b"));
  }

  public void test_evaluation03() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1 + 4; let b = 5; let c = a > b; expose(a, b, c);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(5, context.getVariable("a"));
    assertEquals(5, context.getVariable("b"));
    assertEquals(false, context.getVariable("c"));
  }

  public void test_evaluation04() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1 > 4; let b = 5 >= 5; let c = a <= b; expose(a, b, c);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(false, context.getVariable("a"));
    assertEquals(true, context.getVariable("b"));
    assertEquals(true, context.getVariable("c"));
  }

  public void test_evaluation05() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1; if (let b = 5 > 1) {a = 5; } expose(a);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(5, context.getVariable("a"));
    assertEquals(false, context.exist("b"));
  }

  public void test_evaluation06() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1; while ( a < 5 ) { a++; } expose(a);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(5, context.getVariable("a"));
    assertEquals(false, context.exist("b"));
  }

  public void test_evaluation07() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = False; a = !a; expose(a);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(true, context.getVariable("a"));
  }

  public void test_evaluation08() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1; let b = a + - 1; expose(a, b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(1, context.getVariable("a"));
    assertEquals(0, context.getVariable("b"));
  }

  public void test_evaluation09() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 1; let b = a ++; let c = ++ a; expose(a, b, c);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(3, context.getVariable("a"));
    assertEquals(1, context.getVariable("b"));
    assertEquals(3, context.getVariable("c"));
  }

  public void test_evaluation10() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 3; let b = a --; let c = -- a; expose(a, b, c);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(1, context.getVariable("a"));
    assertEquals(3, context.getVariable("b"));
    assertEquals(1, context.getVariable("c"));
  }

  public void test_evaluation11() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 3; let c = 1; let b = a < 3 ? 2 + 3 : c > 1 ? 2 : 1; expose(b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(1, context.getVariable("b"));
  }

  public void test_evaluation12() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 3; let b = 1; b += a; expose(b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(4, context.getVariable("b"));
  }

  public void test_evaluation13() {
    EvaluationContext context = new EvaluationContext(true);
    Expression ex = new ExpressionParser().parse("let a = 3.0; let b = 1; b += a; expose(b);");
    ExpressionEngine engine = new ExpressionEngine();
    engine.evaluate(context, ex);
    assertEquals(4.0, context.getVariable("b"));
  }
}
