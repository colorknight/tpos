package org.datayoo.tpos;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * time position
 *
 * @author Taiding Tang
 */
public class TposExpression implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String CURRENT_YEAR = "$CURRENTYEAR";
  public static final String CURRENT_MONTH = "$CURRENTMONTH";
  public static final String CURRENT_WEEK = "$CURRENTWEEK";
  public static final String CURRENT_DAY = "$CURRENTDAY";
  public static final String CURRENT_HOUR = "$CURRENTHOUR";
  public static final String CURRENT_MINUTE = "$CURRENTMINUTE";
  public static final String CURRENT_POSITION = "$NOW";
  public static final String TIME_PATTERN = "^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}.*";
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String[] expressionTypes = new String[] { CURRENT_YEAR,
      CURRENT_MONTH, CURRENT_WEEK, CURRENT_DAY, CURRENT_HOUR, CURRENT_MINUTE,
      CURRENT_POSITION
  };

  public static final char YEAR = 'Y';
  public static final char MONTH = 'M';
  public static final char DAY = 'D';
  public static final char WEEK = 'W';
  public static final char HOUR = 'H';
  public static final char MINUTE = 'I';// MI
  public static final char SECOND = 'S';

  public static final char PLUS = '+';
  public static final char MINUS = '-';

  protected Pattern pattern = Pattern.compile(TIME_PATTERN);

  protected int expressionTypeIndex;

  protected String anchorExpression;

  protected Date anchorDate;

  protected String adjustExpression;

  public TposExpression(String expression) {
    Validate.notEmpty(expression, "expression is empty!");
    expression = expression.trim();
    expressionTypeIndex = checkExpression(expression);
    splitExression(expression);
  }

  protected int checkExpression(String expression) {
    expression = expression.toUpperCase();
    for (int i = 0; i < expressionTypes.length; i++) {
      if (expression.startsWith(expressionTypes[i])) {
        return i;
      }
    }
    Matcher matcher = pattern.matcher(expression);
    if (matcher.matches())
      return -1;
    throw new IllegalArgumentException(
        "Invalid expression! Should be started with '$CurrentYear',"
            + "'$CurrentMonth','$CurrentWeek','$CurrentDay','$CurrentHour','$CurrentMinute','$Now' "
            + " or 'yyyy-MM-dd HH:mm:ss' and case insenstive!");
  }

  protected void splitExression(String expression) {
    if (expressionTypeIndex != -1) {
      anchorExpression = expressionTypes[expressionTypeIndex];
      adjustExpression = expression
          .substring(expressionTypes[expressionTypeIndex].length());
    } else {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
          DEFAULT_DATE_FORMAT);
      try {
        anchorDate = simpleDateFormat.parse(expression.substring(0, 19));
      } catch (ParseException e) {
        throw new IllegalArgumentException("Invalid date format!", e);
      }
      adjustExpression = expression.substring(19);
    }
    adjustExpression = adjustExpression.toUpperCase();
  }

  public Date getDate() {
    Calendar calendar = Calendar.getInstance();
    if (anchorDate != null) {
      calendar.setTime(anchorDate);
    } else {
      anchor(calendar, expressionTypeIndex);
    }
    position(calendar, adjustExpression); // offset
    return calendar.getTime();
  }

  public static Date position(Date date, String adjustExpression) {
    Validate.notNull(date, "date is null!");
    Validate.notEmpty(adjustExpression, "adjustExpression is empty!");
    adjustExpression = adjustExpression.toUpperCase();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    position(calendar, adjustExpression);
    return calendar.getTime();
  }

  protected static void position(Calendar calendar, String expression) {
    char operate = ' ', flag = ' ';
    int num = -1;
    for (int i = 0; i < expression.length(); i++) {
      char ch = expression.charAt(i);
      if (ch == ' ')
        continue;
      if (ch == PLUS || ch == MINUS) {
        operate = ch;
      } else if (Character.isDigit(ch)) {
        int j = i + 1;
        for (; j < expression.length(); j++) {
          ch = expression.charAt(j);
          if (!Character.isDigit(ch)) {
            break;
          }
        }
        if (j == expression.length()) {
          throw new IllegalArgumentException("Invalid expression");
        }
        String sub = expression.substring(i, j);
        num = Integer.valueOf(sub).intValue();
        i = j - 1;
      } else if (ch == YEAR || ch == MONTH || ch == DAY || ch == WEEK
          || ch == HOUR || ch == SECOND) {
        if (ch == MONTH) {
          if (i + 1 < expression.length()
              && expression.charAt(i + 1) == MINUTE) {
            ch = MINUTE;
            i++;
          }
        }
        flag = ch;
        calc(calendar, operate, flag, num);
        operate = ' ';
        flag = ' ';
        num = -1;
      }
    }
  }

  protected static void calc(Calendar calendar, char operate, char flag,
      int num) {
    if (operate == PLUS) {
      plus(calendar, flag, num);
    } else if (operate == MINUS) {
      minus(calendar, flag, num);
    } else {
      throw new IllegalArgumentException("Invalid operate!");
    }
  }

  protected static void plus(Calendar calendar, char flag, int num) {
    switch (flag) {
      case YEAR:
        int year = calendar.get(Calendar.YEAR);
        year += num;
        calendar.set(Calendar.YEAR, year);
        break;
      case MONTH:
        int month = calendar.get(Calendar.MONTH);
        month += num;
        calendar.set(Calendar.MONTH, month);
        break;
      case WEEK:
        num *= 7;
      case DAY:
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        day += num;
        calendar.set(Calendar.DAY_OF_YEAR, day);
        break;
      case HOUR:
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour += num;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        break;
      case MINUTE:
        int minute = calendar.get(Calendar.MINUTE);
        minute += num;
        calendar.set(Calendar.MINUTE, minute);
        break;
      case SECOND:
        int second = calendar.get(Calendar.SECOND);
        second += num;
        calendar.set(Calendar.SECOND, second);
        break;
      default:
        throw new IllegalArgumentException("Invalid flag!");
    }
  }

  protected static void minus(Calendar calendar, char flag, int num) {
    switch (flag) {
      case YEAR:
        int year = calendar.get(Calendar.YEAR);
        year -= num;
        calendar.set(Calendar.YEAR, year);
        break;
      case MONTH:
        int month = calendar.get(Calendar.MONTH);
        month -= num;
        calendar.set(Calendar.MONTH, month);
        break;
      case WEEK:
        num *= 7;
      case DAY:
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        day -= num;
        calendar.set(Calendar.DAY_OF_YEAR, day);
        break;
      case HOUR:
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour -= num;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        break;
      case MINUTE:
        int minute = calendar.get(Calendar.MINUTE);
        minute -= num;
        calendar.set(Calendar.MINUTE, minute);
        break;
      case SECOND:
        int second = calendar.get(Calendar.SECOND);
        second -= num;
        calendar.set(Calendar.SECOND, second);
        break;
      default:
        throw new IllegalArgumentException("Invalid flag!");
    }
  }

  protected static void anchor(Calendar calendar, int expressionTypeIndex) {
    switch (expressionTypeIndex) {
      case 0:
        calendar.set(Calendar.MONTH, 0);
      case 1:
        calendar.set(Calendar.DAY_OF_MONTH, 1);
      case 3:
        calendar.set(Calendar.HOUR_OF_DAY, 0);
      case 4:
        calendar.set(Calendar.MINUTE, 0);
      case 5:
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        break;
      case 2:
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
          calendar.set(Calendar.DAY_OF_YEAR,
              calendar.get(Calendar.DAY_OF_YEAR) - 7);
        }
        calendar.set(Calendar.DAY_OF_WEEK, 2); // Monday
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        break;
    }
  }

  public String getAnchorExpression() {
    return anchorExpression;
  }

  public String getAdjustExpression() {
    return adjustExpression;
  }

}
