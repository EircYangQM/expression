package org.xq.parser;

import org.xq.parser.expressions.*;
import org.xq.parser.token.Kind;
import org.xq.parser.token.Token;
import org.xq.parser.token.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
  public Expression parse(String input) {
    Tokenizer tokenizer = new Tokenizer(input);
    return parseScopeExpression(tokenizer, false);
  }

  private ScopeExpression parseScopeExpression(Tokenizer tokenizer, boolean withBracket) {
    ScopeExpression mainScope = new ScopeExpression(!withBracket);
    Expression current;
    if (withBracket) {
      tokenizer.verifyNext("{");
    }
    while (true) {
      Token token = tokenizer.lookNext();
      if (token.getKind() == Kind.Keyword) {
        if (token.equalsValue("if")) {
          current = parseIfExpression(tokenizer);
        } else if (token.equalsValue("while")) {
          current = parseWhileExpression(tokenizer);
        } else {
          current = parseSingleExpression(tokenizer, true);
        }
      } else if (token.getKind() == Kind.StartBracket || token.equalsValue("{")) {
        current = parseScopeExpression(tokenizer, true);
      } else {
        current = parseSingleExpression(tokenizer, true);
      }

      token = tokenizer.lookNext();
      mainScope.addExpression(current);
      if (withBracket) {
        if (token.equalsValue("}")) {
          tokenizer.next();
          break;
        }

        if (Tokenizer.EOF.equals(token)) {
          throw new RuntimeException("Unexpected EOF in scope statement.");
        }
      } else {
        if (Tokenizer.EOF.equals(token)) {
          break;
        }
      }
    }

    return mainScope;
  }

  private Expression parseSingleExpression(Tokenizer tokenizer, boolean withSemicolon) {
    Token token = tokenizer.lookNext();
    boolean quoted = false;
    if (token.getKind() == Kind.StartBracket && token.equalsValue("(")) {
      tokenizer.next();
      token = tokenizer.lookNext();
      quoted = true;
    }

    Expression current;
    boolean isDeclareExpression = false;
    if (token.equalsValue("let")) {
      tokenizer.next();
      isDeclareExpression = true;
    }

    current = parseCalculateExpression(tokenizer);
    token = tokenizer.lookNext();
    if (token.getKind() == Kind.Equals) {
      tokenizer.next();
      current = new AssignExpression(current, parseCalculateExpression(tokenizer));
    }

    if (quoted) {
      tokenizer.verifyNext(")");
    }

//    token = tokenizer.lookNext();
//    if (token.equalsValue(";")) {
//      tokenizer.next();
//    }
    if (withSemicolon) {
      tokenizer.verifyNext(";");
    }

    if (isDeclareExpression) {
      current = new DeclareExpression(current);
    }
    return current;
  }

  private Expression parseIfExpression(Tokenizer tokenizer) {
    List<Expression> conditions = new ArrayList<>();
    List<Expression> scopes = new ArrayList<>();
    Expression elseExpr = null;
    boolean isFirst = true;
    boolean skipCondition = false;
    while (true) {
      Token token = tokenizer.lookNext();
      if (isFirst) {
        tokenizer.verifyNext("if");
        isFirst = false;
      } else {
        if (token.equalsValue("else")) {
          tokenizer.next();
          token = tokenizer.next();
          if (token.equalsValue("if")) {
            skipCondition = false;
          } else {
            skipCondition = true;
          }
        } else {
          break;
        }
      }

      Expression condition = null;
      if (!skipCondition) {
        condition = parseSingleExpression(tokenizer, false);
      }

      token = tokenizer.lookNext();
      ScopeExpression scope;
      if (token.getKind() == Kind.StartBracket && token.equalsValue("{")) {
        scope = parseScopeExpression(tokenizer, true);
      } else {
        scope = parseScopeExpression(tokenizer, false);
        if (scope.getExpressions().size() > 1) {
          throw new RuntimeException("Please quote the expression if the expression more than one.");
        }
      }

      if (condition == null) {
        elseExpr = scope;
        break;
      } else {
        conditions.add(condition);
        scopes.add(scope);
      }
    }

    return new IfExpression(conditions, scopes, elseExpr);
  }

  private Expression parseWhileExpression(Tokenizer tokenizer) {
    tokenizer.next();
    Expression condition = parseSingleExpression(tokenizer, false);
    ScopeExpression scope = parseScopeExpression(tokenizer, true);
    return new WhileExpression(condition, scope);
  }

  private Expression parseCalculateExpression(Tokenizer tokenizer) {
    List<Expression> subExpressions = new ArrayList<>();
    Token token = tokenizer.lookNext();
    boolean quoted = false;
    if (token.getKind() == Kind.StartBracket && token.equalsValue("(")) {
      tokenizer.next();
      quoted = true;
    }
    while (true) {
      token = tokenizer.lookNext();
      if (token.getKind() == Kind.StartBracket && token.equalsValue("(")) {
        subExpressions.add(parseCalculateExpression(tokenizer));
      } else {
        subExpressions.add(parseValuableExpression(tokenizer));
      }

      token = tokenizer.lookNext();

      if (token.getKind() == Kind.EndSegment) {
        break;
      } else if (token.getKind() == Kind.Keyword) {
        break;
      } else if (token.getKind() == Kind.Equals) {
        break;
      } else if (token.getKind() == Kind.Separator && token.equalsValue(",")) {
        break;
      } else if (token.getKind() == Kind.EndBracket && token.equalsValue(")")) {
        break;
      }
    }
    if (quoted) {
      tokenizer.verifyNext(")");
    }

    if (subExpressions.size() == 1) {
      return subExpressions.get(0);
    } else {
      return new ValueExpression(subExpressions, quoted);
    }
  }

  private Expression parseValuableExpression(Tokenizer tokenizer) {
    Expression current = null;
    Expression parent = null;
    Token token = tokenizer.lookNext();
    if (token.getKind() == Kind.Identity) {
      while (true) {
        token = tokenizer.next();
        String identityValue = token.getValue();
        token = tokenizer.lookNext();
        if (token.getKind() == Kind.StartBracket) {
          if (token.equalsValue("(")) {
            List<Expression> params = parseParamsExpressions(tokenizer, false);
            current = new FunctionExpression(identityValue, params);
          } else if (token.equalsValue("[")) {
            List<Expression> params = parseParamsExpressions(tokenizer, true);
            current = new AccessorExpression(new VariableExpression(identityValue), params, true);
          }
        } else {
          current = new VariableExpression(identityValue);
        }

        token = tokenizer.lookNext();
        if (token.getKind() == Kind.Pointer) {
          tokenizer.next();
          if (parent != null) {
            List<Expression> params = new ArrayList<>();
            params.add(current);
            current = new AccessorExpression(parent, params, false);
          }
          parent = current;
        } else {
          break;
        }
      }
      if (parent != null) {
        List<Expression> params = new ArrayList<>();
        params.add(current);
        current = new AccessorExpression(parent, params, false);
      }
    } else if (token.getKind() == Kind.Keyword) {
      if (token.equalsValue("false") || token.equalsValue("true")) {
        tokenizer.next();
        current = new ConstantExpression(token.getValue(), ConstantExpression.TYPE_BOOL);
      }
    } else if (token.getKind() == Kind.Keyword && token.equalsValue("true")) {
      tokenizer.next();
      current = new ConstantExpression(token.getValue(), ConstantExpression.TYPE_NUMBER);
    } else if (token.getKind() == Kind.Number) {
      tokenizer.next();
      current = new ConstantExpression(token.getValue(), ConstantExpression.TYPE_NUMBER);
    } else if (token.getKind() == Kind.String) {
      tokenizer.next();
      current = new ConstantExpression(token.getValue(), ConstantExpression.TYPE_STRING);
    } else if (token.getKind() == Kind.Operator) {
      tokenizer.next();
      current = new OperatorExpression(token.getValue());
    } else {
      throw new RuntimeException(String.format("Unexpected token in value statement. token: %s", token));
    }

    return current;
  }

  private List<Expression> parseParamsExpressions(Tokenizer tokenizer, boolean indexed) {
    tokenizer.next();
    List<Expression> expressions = new ArrayList<>();
    while (true) {
      expressions.add(parseCalculateExpression(tokenizer));
      Token token = tokenizer.lookNext();
      if (token.equalsValue(",")) {
        tokenizer.next();
      } else if (token.equalsValue(indexed ? "]" : ")")) {
        tokenizer.next();
        break;
      } else {
        throw new RuntimeException(String.format("Unexpected token value %s.", token.getValue()));
      }
    }
    return expressions;
  }


}


