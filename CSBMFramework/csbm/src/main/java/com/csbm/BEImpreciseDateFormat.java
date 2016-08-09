package com.csbm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

/**
 * Used to csbm legacy created_at and updated_at from disk. It is only precise to the second.
 */

/** package */ class BEImpreciseDateFormat {
  private static final String TAG = "BEDateFormat";

  private static final BEImpreciseDateFormat INSTANCE = new BEImpreciseDateFormat();
  public static BEImpreciseDateFormat getInstance() {
    return INSTANCE;
  }

  // SimpleDateFormat isn't inherently thread-safe
  private final Object lock = new Object();

  private final DateFormat dateFormat;

  private BEImpreciseDateFormat() {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    format.setTimeZone(new SimpleTimeZone(0, "GMT"));
    dateFormat = format;
  }

  /* package */ Date parse(String dateString) {
    synchronized (lock) {
      try {
        return dateFormat.parse(dateString);
      } catch (java.text.ParseException e) {
        // Should never happen
        PLog.e(TAG, "could not parse date: " + dateString, e);
        return null;
      }
    }
  }

  /* package */ String format(Date date) {
    synchronized (lock) {
      return dateFormat.format(date);
    }
  }
}
