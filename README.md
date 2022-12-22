# Expression

**A java library to parse and execute script**

[Key Features](#key-features) |
[Sample](#sample) 


## Key Features
Expression offers full features for the script language:
* Parse the script to expression instance
* External functions support.

## Sample

```java
EvaluationContext context = new EvaluationContext(true);
Expression ex = new ExpressionParser().parse("let a = 3; let c = 1; let b = a < 3 ? 2 + 3 : c > 1 ? 2 : 1; expose(b);");
ExpressionEngine engine = new ExpressionEngine();
engine.evaluate(context, ex);
System.out.println("b value is " + context.getVariable("b"));
```