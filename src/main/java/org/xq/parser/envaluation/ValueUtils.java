package org.xq.parser.envaluation;

public class ValueUtils {
  public static Long parseAsLong(final String s) {
    try {
      return Long.valueOf(s);
    } catch (NumberFormatException numberFormatException) {
      return null;
    }
  }

  public static Integer parseAsInteger(final String s) {
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException numberFormatException) {
      return null;
    }
  }

  public static Double parseAsDouble(final String s) {
    try {
      return Double.valueOf(s);
    } catch (NumberFormatException numberFormatException) {
      return null;
    }
  }

  public static Boolean parseAsBool(final String s) {
    try {
      return Boolean.valueOf(s);
    } catch (NumberFormatException numberFormatException) {
      return null;
    }
  }
}
