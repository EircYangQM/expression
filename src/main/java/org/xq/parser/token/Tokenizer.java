package org.xq.parser.token;

import java.util.Arrays;

public class Tokenizer {
  public static final Token EOF = new Token(Kind.EOF);
  public static final char[] OperatorChars = new char[] {'+', '-', '*', '/', '%', '&', '|', '$', '!', '~', '^', '>', '<', '=', '?', ':'};
  public static final String[] DEFAULT_KEYWORDS = new String[] {"if", "else", "while", "for", "foreach", "let", "true", "false"};
  public static final char QUOTA = '"';

  private int current;
  private String input;
  private int length;
  private String[] keywords;

  public Tokenizer(String input) {
    this.current = 0;
    this.input = input;
    this.length = input == null ? 0 : input.length();
    this.keywords = DEFAULT_KEYWORDS;
  }

  public Token next() {
    return nextInternal(true);
  }

  public Token lookNext() {
    return nextInternal(false);
  }

  public void verifyNext(String value) {
    Token token = next();
    if (!token.equalsValue(value)) {
      throw new RuntimeException(String.format("Excepted: '%s', Actual: '%s'", value, token.getValue()));
    }
  }

//  public Token LookNext2() {
//    int original = this.current;
//    nextInternal(true);
//    Token next2 = nextInternal(true);
//    this.current = original;
//    return next2;
//  }

  private Token nextInternal(boolean moveNext) {
    int original = this.current;
    int currentPos = original;
    if (currentPos == length) {
      return EOF;
    }

    char ch = this.input.charAt(currentPos);
    while (Character.isWhitespace(ch) || ch == '\t' || ch == '\r' || ch == '\n') {
      currentPos ++;
      ch = this.input.charAt(currentPos);
    }

    Kind kind;
    int pos;
    String value = null;
    if (isValidIdentityStart(ch)) {
      pos = readIdentity((c) -> isValidIdentity(c), currentPos,false);
      value = input.substring(currentPos, pos);
      if (Arrays.stream(keywords).anyMatch(value.toLowerCase()::equals)) {
        kind = Kind.Keyword;
      } else {
        kind = Kind.Identity;
      }
    } else if (isOperation(ch)) {
      if (ch == '=' && currentPos + 1 < length && this.input.charAt(currentPos + 1) != '=') {
        kind = Kind.Equals;
        pos = currentPos + 1;
      } else {
        kind = Kind.Operator;
        pos = readIdentity((c) -> isOperation(c), currentPos,false);
      }
    } else if (isNumberStart(ch)) {
      kind = Kind.Number;
      pos = readIdentity((c) -> isNumber(c), currentPos,false);
    } else if (ch == QUOTA) {
      kind = Kind.String;
      pos = readIdentity((c) -> c != '\r' && c != '\n', currentPos,true);
    } else if (ch == '(' || ch == '[' || ch == '{') {
      kind = Kind.StartBracket;
      pos = currentPos + 1;
    } else if (ch == ')' || ch == ']' || ch == '}') {
      kind = Kind.EndBracket;
      pos = currentPos + 1;
    } else if (ch == ',') {
      kind = Kind.Separator;
      pos = currentPos + 1;
    } else if (ch == '.') {
      kind = Kind.Pointer;
      pos = currentPos + 1;
    } else if (ch == ';') {
      kind = Kind.EndSegment;
      pos = currentPos + 1;
    } else {
      throw new RuntimeException(
          String.format("The '%s' char is invalid at %s.", ch,
              input.substring(currentPos - 5 < 0 ? 0 : currentPos - 5), currentPos + 5 > length ? length : currentPos + 5));
    }

    if (value == null) {
      value = input.substring(currentPos, pos);
    }
    if (moveNext) {
      this.current = pos;
    } else {
      this.current = original;
    }
    return new Token(kind, value);
  }

  private boolean isValidIdentityStart(char ch) {
    return Character.isAlphabetic(ch) || ch == '_';
  }

  private boolean isValidIdentity(char ch) {
    return Character.isAlphabetic(ch) || ch == '_' || Character.isDigit(ch);
  }

  private boolean isOperation(char ch) {
    return contains(OperatorChars, ch);
  }

  private boolean isNumber(char ch) {
    return Character.isDigit(ch) || ch == '-' || ch == '+' || ch == 'E' || ch == 'e' || ch == '.';
  }

  private boolean isNumberStart(char ch) {
    return Character.isDigit(ch) || ch == '-' || ch == '+';
  }

  private int readIdentity(CheckFunction checkFunction, int pos, boolean acceptEscape) {
    int idx = pos;
    char ch;
    while (true) {
      if (idx == length) {
        return idx;
      }
      ch = this.input.charAt(idx);
      if (ch == QUOTA) {
        if (!acceptEscape) {
          return idx;
        }

        if (idx + 1 < length && this.input.charAt(idx) != QUOTA) {
          return idx;
        }
      }

      if (!checkFunction.isValidChar(ch)) {
        return idx;
      }
      idx ++;
    }
  }

  private static boolean contains(char[] array, char target) {
    for (char value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }
}

interface CheckFunction {
  boolean isValidChar(char ch);
}

