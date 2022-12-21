package org.xq.expression.token;

public enum Kind {
  Identity,
  Operator,
  Keyword,
  String,
  EOF,
  StartBracket,
  EndBracket,
  Separator,
  Equals,
  Pointer,
  Number,
  EndSegment,
}
