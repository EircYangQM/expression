package org.xq.parser.token;

public class Token {
  private final Kind kind;
  private final String value;

  public Token(Kind kind) {
    this(kind, null);
  }

  public Token(Kind kind, String value) {
    this.kind = kind;
    this.value = value;
  }

  public Kind getKind() {
    return kind;
  }

  public String getValue() {
    return value;
  }

  public boolean equalsValue(String value) {
    return value != null && value.equalsIgnoreCase(this.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Token) {
      Token t = (Token) obj;
      if (value != null) {
        return t.kind == kind && value.equals(t.value);
      } else {
        return t.kind == kind && t.value == null;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "K: " + kind + ", V: " + value;
  }
}
