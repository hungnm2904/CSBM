package com.csbm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import bolts.Task;

/** package */ abstract class BESQLiteOpenHelper {

  private final SQLiteOpenHelper helper;

  public BESQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                            int version) {
    helper = new SQLiteOpenHelper(context, name, factory, version) {
      @Override
      public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        BESQLiteOpenHelper.this.onOpen(db);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
        BESQLiteOpenHelper.this.onCreate(db);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        BESQLiteOpenHelper.this.onUpgrade(db, oldVersion, newVersion);
      }
    };
  }

  public Task<BESQLiteDatabase> getReadableDatabaseAsync() {
    return getDatabaseAsync(false);
  }

  public Task<BESQLiteDatabase> getWritableDatabaseAsync() {
    return getDatabaseAsync(true);
  }

  private Task<BESQLiteDatabase> getDatabaseAsync(final boolean writable) {
    return BESQLiteDatabase.openDatabaseAsync(
        helper, !writable ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
  }

  public void onOpen(SQLiteDatabase db) {
    // do nothing
  }

  public abstract void onCreate(SQLiteDatabase db);
  public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
