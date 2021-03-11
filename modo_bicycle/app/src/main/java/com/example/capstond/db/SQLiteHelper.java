package com.example.capstond.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG_Time = "Time";
    public static final String TAG_LastTime = "LastTime";
    public static final String TAG_DistDif = "DistDif";
    public static final String TAG_CheckTime = "CheckTime";

    public static final String TAG_Lati = "Lati";
    public static final String TAG_Longti = "Longti";


    private static final String TAG_TABLE_NAME = "getaccel2";


    // 데이터베이스
    private static final String DATABASE_NAME      = "test.db";
    private static final int DATABASE_VERSION      = 1;

    // 테이블
    public static final String TABLE_NAME       = "imageTb";
    public static final String COLUMN_ID        = "id";
    public static final String COLUMN_DATE      = "date1";
    public static final String COLUMN_FEEL      = "feels";
    public static final String COLUMN_IMAGE     = "image";
    public static final String COLUMN_TEXT      = "text";

    private static final String DATABASE_CREATE_TEAM = "create table "
            + TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + TAG_Time + " text, "
            + TAG_LastTime + " text, "
            + TAG_DistDif + " text, "
            + TAG_CheckTime + " text, "
            + TAG_Lati + " text, "
            + TAG_Longti + " text);";


// 기존 테이블에 레코드 추가시 사용
//    private static final String DATABASE_ALTER_TEAM_1 = "ALTER TABLE "
//            + TABLE_TEAM + " ADD COLUMN " + COLUMN_COACH + " string;";
//
//    private static final String DATABASE_ALTER_TEAM_2 = "ALTER TABLE "
//            + TABLE_TEAM + " ADD COLUMN " + COLUMN_STADIUM + " string;";


//    public SQLiteHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
//        super(context, name, factory, version);
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // 앱을 삭제후 앱을 재설치하면 기존 DB파일은 앱 삭제시 지워지지 않기 때문에
        // 테이블이 이미 있다고 생성 에러남
        // 앱을 재설치시 데이터베이스를 삭제해줘야함.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(DATABASE_CREATE_TEAM);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

        //기존 테이블에 레코드 추가시 사용
//        if (oldVersion < 2) {
//            db.execSQL(DATABASE_ALTER_TEAM_1);
//        }
//        if (oldVersion < 3) {
//            db.execSQL(DATABASE_ALTER_TEAM_2);
//        }
    }

    public void insertAllDatas1() {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(TAG_Time, "2");
            cv.put(TAG_LastTime, "2");
            cv.put(TAG_DistDif, "2");
            cv.put(TAG_CheckTime, "2");
//                cv.put(TAG_LastTime, tempMap.get(TAG_LastTime));
//                cv.put(TAG_DistDif, tempMap.get(TAG_DistDif));
//                cv.put(TAG_CheckTime, tempMap.get(TAG_CheckTime));
            db.insert("imageTb", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void insertAllDatas2(String a, String b) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(TAG_Time, "2");
            cv.put(TAG_LastTime, a);
            cv.put(TAG_DistDif, b);
            cv.put(TAG_CheckTime, "2");
//                cv.put(TAG_LastTime, tempMap.get(TAG_LastTime));
//                cv.put(TAG_DistDif, tempMap.get(TAG_DistDif));
//                cv.put(TAG_CheckTime, tempMap.get(TAG_CheckTime));
            db.insert("imageTb", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void insertAllDatas3(String a, String b, String c, String d, String e) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(TAG_Time, a);
            cv.put(TAG_LastTime, b);
            cv.put(TAG_DistDif, c);
            cv.put(TAG_CheckTime, d);
            cv.put(TAG_Lati, d);
            cv.put(TAG_Longti, e);
            Log.d("", "SQL insert - " + cv);
//                cv.put(TAG_LastTime, tempMap.get(TAG_LastTime));
//                cv.put(TAG_DistDif, tempMap.get(TAG_DistDif));
//                cv.put(TAG_CheckTime, tempMap.get(TAG_CheckTime));
            db.insert("imageTb", null, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void insertAllDatas(ArrayList<HashMap<String, String>> datas) {

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < datas.size(); i++) {
                HashMap<String, String> tempMap = datas.get(i);
                ContentValues cv = new ContentValues();
                cv.put(TAG_Time, tempMap.get(TAG_Time));
                cv.put(TAG_LastTime, tempMap.get(TAG_LastTime));
                cv.put(TAG_DistDif, tempMap.get(TAG_DistDif));
                cv.put(TAG_CheckTime, tempMap.get(TAG_CheckTime));
                db.insert("imageTb", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAllContacts(String rentalTime, String returnTime) {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();

        // Select All Query
        //String selectQuery = "SELECT  * FROM " + TAG_TABLE_NAME;

        SimpleDateFormat format1 = new SimpleDateFormat( "HH:mm:ss");
        //String time1 = "01";
        String time2 = "2019/12/08 02:00:00";
        String time3 = "2019/12/08 02:40:00";


        //String selectQuery = "SELECT * FROM " + TAG_TABLE_NAME +  " WHERE " + "Time" +  " BETWEEN " + time2 + " AND " + time3;
//        String selectQuery1 = "SELECT * FROM getaccel2 WHERE Time BETWEEN " + '"' + rentalTime + '"' + " AND " + '"' +returnTime+'"' ;
//        String selectQuery1 = "SELECT * FROM imageTb WHERE Time BETWEEN " + '"' + rentalTime + '"' + " AND " + '"' +returnTime+'"' ;
        String selectQuery2 = "SELECT * FROM imageTb WHERE Time BETWEEN " + "'" + rentalTime + "'" + " AND " + "'" +returnTime+"'" ;

        SQLiteDatabase db = this.getWritableDatabase();
//        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery2, null);

        // looping through all rows and adding to list
        while (cursor.moveToNext()) {
            HashMap<String,String> hashMap = new HashMap<>();

            Log.d("", "1 : " + cursor.getColumnIndex(TAG_Time));
            Log.d("", "2 : " + cursor.getColumnIndex(TAG_LastTime));
            Log.d("", "3 : " + cursor.getColumnIndex(TAG_DistDif));
            Log.d("", "4 : " + cursor.getColumnIndex(TAG_CheckTime));

//            int time_1 = cursor.getColumnIndex(TAG_Time);


            hashMap.put(TAG_Time, cursor.getString(cursor.getColumnIndex(TAG_Time)));
            hashMap.put(TAG_LastTime, cursor.getString(cursor.getColumnIndex(TAG_LastTime)));
            hashMap.put(TAG_DistDif, cursor.getString(cursor.getColumnIndex(TAG_DistDif)));
            hashMap.put(TAG_CheckTime, cursor.getString(cursor.getColumnIndex(TAG_CheckTime)));
            hashMap.put(TAG_Lati, cursor.getString(cursor.getColumnIndex(TAG_Lati)));
            hashMap.put(TAG_Longti, cursor.getString(cursor.getColumnIndex(TAG_Longti)));

//            hashMap.put(TAG_Time, cursor.getString(0));
//            hashMap.put(TAG_LastTime, cursor.getString(1));
//            hashMap.put(TAG_DistDif, cursor.getString(2));
//            hashMap.put(TAG_CheckTime, cursor.getString(3));
//            HashMap<String, String> tempMap = datas.get(i);

//            ContentValues cv = new ContentValues();
//            cv.put(TAG_Time, tempMap.get(TAG_Time));
//            cv.put(TAG_LastTime, tempMap.get(TAG_LastTime));
//            cv.put(TAG_DistDif, tempMap.get(TAG_DistDif));
//            cv.put(TAG_CheckTime, tempMap.get(TAG_CheckTime));
//            db.insert("imageTb", null, cv);

            // Adding contact to list
            contactList.add(hashMap);
        }

        // return contact list
        return contactList;
    }
}