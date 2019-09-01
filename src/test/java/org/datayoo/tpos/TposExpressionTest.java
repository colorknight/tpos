package org.datayoo.tpos;

import junit.framework.TestCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TposExpressionTest extends TestCase {

  public void testExpression() {
    String exp = "$NOW + 1M";
    TposExpression tposExpression = new TposExpression(exp);
    printDate(tposExpression.getDate());
    exp = "$CURRENTMONTH + 1M";
    tposExpression = new TposExpression(exp);
    printDate(tposExpression.getDate());
    exp = "$CURRENTYEAR + 13Y";
    tposExpression = new TposExpression(exp);
    printDate(tposExpression.getDate());

    exp = "$CURRENTWEEK -1W";
    tposExpression = new TposExpression(exp);
    printDate(tposExpression.getDate());

    exp = "2018-07-28 03:24:23  +1y +1m +4d +9h -4mi -12s";
    tposExpression = new TposExpression(exp);
    printDate(tposExpression.getDate());
  }

  public void testRelativeExpression() {
    String srcStr = "2019-09-08 00:00:00";
    String dstStr = "2019-11-03 08:20:15";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      Date srcDate = dateFormat.parse(srcStr);
      Date dstDate = dateFormat.parse(dstStr);
      String exp = "+ 2M -5d +8h +20mi +15s";
      Date adjustedDate = TposExpression.position(srcDate, exp);
      printDate(adjustedDate);
      assert (dstDate.equals(adjustedDate));
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  protected void printDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    System.out.println(dateFormat.format(date));
  }

}
