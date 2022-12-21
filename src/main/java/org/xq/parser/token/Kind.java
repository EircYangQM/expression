package org.xq.parser.token;

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
