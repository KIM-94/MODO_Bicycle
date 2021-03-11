package com.example.capstond.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG_Time = "Time";
    public static final String TAG_LastTime = "LastTime";
    public static final String TAG_DistDif = "DistDif";
    public static final String TAG_CheckTime = "CheckTime";

    private static final String TAG_TABLE_NAME = "getaccel2";

    // 테이블
    public static final String TABLE_NAME       = "imageTb";
    public static final String COLUMN_ID        = "id";
    public static final String COLUMN_DATE      = "date1";
    public static final String COLUMN_FEEL      = "feels";
    public static final String COLUMN_IMAGE     = "image";
    public static final String COLUMN_TEXT      = "text";

    private static final String DATABASE_CREATE_TEAM = "create table "
            + TAG_TABLE_NAME + "(" + TAG_Time + " TEXT, "
            + TAG_LastTime + " TEXT, "
            + TAG_DistDif + " TEXT, "
            + TAG_CheckTime + " TEXT);";


    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
//        super(context, name, factory, version);
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        /* 이름은 DEMO_SQLITE이고, 자동으로 값이 증가하는 _id 정수형 기본키 컬럼과
        item 문자열 컬럼, price 문자열 컬럼, create_at 문자열 컬럼으로 구성된 테이블을 생성. */
//        db.execSQL( "CREATE TABLE DEMO_SQLITE (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "item TEXT, price TEXT, create_at TEXT);");
//        String sql = "CREATE TABLE getaccel2 (Time text, LastTime text, DistDif text, CheckTime text)";
        db.execSQL(DATABASE_CREATE_TEAM);
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String create_at, String item, String price) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        db.execSQL( "INSERT INTO DEMO_SQLITE VALUES(null, " +
                "'" + item + "', " + price + ", '" + create_at + "');");
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
                db.insert("getaccel2", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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
        String selectQuery1 = "SELECT * FROM getaccel2 WHERE Time BETWEEN " + '"' + rentalTime + '"' + " AND " + '"' +returnTime+'"' ;
        String selectQuery2 = "SELECT * FROM getaccel2 WHERE Time BETWEEN " + "'" + time2 + "'" + " AND " + "'" +time3+"'" ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery1, null);

        // looping through all rows and adding to list
        while (cursor.moveToNext()) {
            HashMap<String,String> hashMap = new HashMap<>();

            hashMap.put(TAG_Time, cursor.getString(0));
            hashMap.put(TAG_LastTime, cursor.getString(1));
            hashMap.put(TAG_DistDif, cursor.getString(2));
            hashMap.put(TAG_CheckTime, cursor.getString(3));

            // Adding contact to list
            contactList.add(hashMap);
        }

        // return contact list
        return contactList;
    }

    public void update(String item, String price) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE DEMO_SQLITE SET price=" + price +

                " WHERE item='" + item + "';");
        db.close();
    }

    public void delete(String item) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM DEMO_SQLITE WHERE item='" + item + "';");
        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM DEMO_SQLITE", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getString(1)
                    + " | "
                    + cursor.getInt(2)
                    + "원 "
                    + cursor.getString(3)
                    + "\n";
        }

        return result;
    }
}
