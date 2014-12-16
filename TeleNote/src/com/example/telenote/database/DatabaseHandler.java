package com.example.telenote.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * Local MySQL database
 * 
 */

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "TeleNoteData";

	// Contacts table name
	private static final String TABLE_SCRIPTS = "script";
	private static final String TABLE_USAGE = "usage";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NUMBER = "number";
	private static final String KEY_DATE = "date";
	private static final String KEY_TIME = "time";
	private static final String KEY_SECOND = "second";
	private static final String KEY_INOUTCALL = "incomingoutgoing";
	private static final String KEY_RECORDFILE = "record_file";
	private static final String KEY_TRANSCRIPTION = "transcription";

	private static final String KEY_USAGEID = "usageid";
	private static final String KEY_NUMBEROFUSAGETOTAL = "numberofusagetotal";
	private static final String KEY_NUMBEROFUSAGEMONTH = "numberofusagemonth";
	private static final String KEY_UPDATEMONTH = "updatemonth";

	private final ArrayList<ScriptData> script_list = new ArrayList<ScriptData>();
	private final ArrayList<UsageData> datausage_list = new ArrayList<UsageData>();

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SCRIPTS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NUMBER + " TEXT,"
				+ KEY_DATE + " TEXT," + KEY_TIME + " TEXT," + KEY_SECOND
				+ " TEXT," + KEY_INOUTCALL + " TEXT," + KEY_RECORDFILE
				+ " TEXT," + KEY_TRANSCRIPTION + " TEXT" + ")";
		String CREATE_NUMBEROFUSAGE = "CREATE TABLE " + TABLE_USAGE + "("
				+ KEY_USAGEID + " INTEGER PRIMARY KEY,"
				+ KEY_NUMBEROFUSAGETOTAL + " INTEGER," + KEY_NUMBEROFUSAGEMONTH
				+ " INTEGER," + KEY_UPDATEMONTH + " INTEGER" + ")";

		db.execSQL(CREATE_CONTACTS_TABLE);
		db.execSQL(CREATE_NUMBEROFUSAGE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCRIPTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USAGE);
		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations The following
	 * TABLE_CONTACTS methods are for EachData
	 */

	public void Add_Contact(ScriptData script) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, script.getID());
		values.put(KEY_NUMBER, script.getPhoneNumber()); // Contact Name
		values.put(KEY_DATE, script.getDate()); // Contact Phone
		values.put(KEY_TIME, script.getTime());
		values.put(KEY_SECOND, script.getSecond());
		values.put(KEY_INOUTCALL, script.getInOutComing());
		values.put(KEY_RECORDFILE, script.getRecordFile());
		values.put(KEY_TRANSCRIPTION, script.getTransptText());

		// values.put(KEY_RECORDSTART, contact.getRecordStart());
		// values.put(KEY_RECORDEND, contact.getRecordEnd());// Contact Email
		// Inserting Row
		db.insert(TABLE_SCRIPTS, null, values);
		db.close(); // Closing database connection
	}

	// Getting single contact
	ScriptData Get_Contact(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_SCRIPTS, new String[] { KEY_ID,
				KEY_NUMBER, KEY_DATE, KEY_TIME, KEY_SECOND, KEY_INOUTCALL,
				KEY_RECORDFILE, KEY_TRANSCRIPTION }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		ScriptData contact = new ScriptData(Integer.parseInt(cursor
				.getString(0)), cursor.getString(1), cursor.getString(2),
				cursor.getString(3), cursor.getString(4), cursor.getString(5),
				cursor.getString(6), cursor.getString(7));
		// return contact
		cursor.close();
		db.close();

		return contact;
	}

	// Getting All Contacts
	public ArrayList<ScriptData> Get_Scripts() {
		try {
			script_list.clear();

			// Select All Query
			String selectQuery = "SELECT  * FROM " + TABLE_SCRIPTS;

			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					ScriptData script = new ScriptData();
					script.setID(Integer.parseInt(cursor.getString(0)));
					script.setPhoneNumber(cursor.getString(1));
					script.setDate(cursor.getString(2));
					script.setTime(cursor.getString(3));
					script.setSecond(cursor.getString(4));
					script.setInOutComing(cursor.getString(5));
					script.setRecordFile(cursor.getString(6));
					script.setTranscriptText(cursor.getString(7));
					// Adding contact to list
					script_list.add(script);
				} while (cursor.moveToNext());
			}

			// return contact list
			cursor.close();
			db.close();
			return script_list;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("all_contact", "" + e);
		}

		return script_list;
	}

	// Getting contacts Count
	public int Get_Total_Scripts() {
		String countQuery = "SELECT  * FROM " + TABLE_SCRIPTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

	public void User_UpDate_Transcription(ScriptData script) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, script.getID());
		values.put(KEY_NUMBER, script.getPhoneNumber()); // Contact Name
		values.put(KEY_DATE, script.getDate()); // Contact Phone
		values.put(KEY_TIME, script.getTime());
		values.put(KEY_SECOND, script.getSecond());
		values.put(KEY_INOUTCALL, script.getInOutComing());
		values.put(KEY_RECORDFILE, script.getRecordFile());
		values.put(KEY_TRANSCRIPTION, script.getTransptText());

		// updating row
		db.update(TABLE_SCRIPTS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(script.getID()) });
	}

	// Deleting single contact
	public void Delete_Contact(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCRIPTS, KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	/*
	 * The following database corresponds to transcription service usage data
	 */

	public void Add_Usage(UsageData contact) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_USAGEID, contact.getID());
		values.put(KEY_NUMBEROFUSAGETOTAL, contact.getNumberOfUsageTotal());
		values.put(KEY_NUMBEROFUSAGEMONTH, contact.getNumberOfUsageMonth());
		values.put(KEY_UPDATEMONTH, contact.getUpdateMonth());

		db.insert(TABLE_USAGE, null, values);
		db.close(); // Closing database connection
	}

	public int UpDate_Usage(UsageData contact) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NUMBEROFUSAGETOTAL, contact.getNumberOfUsageTotal());
		values.put(KEY_NUMBEROFUSAGEMONTH, contact.getNumberOfUsageMonth());
		values.put(KEY_UPDATEMONTH, contact.getUpdateMonth());

		// updating row
		return db.update(TABLE_USAGE, values, KEY_USAGEID + " = ?",
				new String[] { String.valueOf(contact.getID()) });
	}

	UsageData Get_UsageData(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_USAGE,
				new String[] { KEY_USAGEID, KEY_NUMBEROFUSAGETOTAL,
						KEY_NUMBEROFUSAGEMONTH, KEY_UPDATEMONTH }, KEY_USAGEID
						+ "=?", new String[] { String.valueOf(id) }, null,
				null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		UsageData contact = new UsageData(cursor.getInt(0), cursor.getInt(1),
				cursor.getInt(2), cursor.getInt(3));
		// return contact
		cursor.close();
		db.close();

		return contact;
	}

	public ArrayList<UsageData> Get_UsageDatas() {
		try {
			datausage_list.clear();

			// Select All Query
			String selectQuery = "SELECT  * FROM " + TABLE_USAGE;

			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					UsageData data = new UsageData();
					data.setID(cursor.getInt(0));
					data.setNumberOfUsageTotal(cursor.getInt(1));
					data.setNumberOfUsageMonth(cursor.getInt(2));
					data.setUpdateMonth(cursor.getInt(3));

					// Adding contact to list
					datausage_list.add(data);
				} while (cursor.moveToNext());
			}

			// return contact list
			cursor.close();
			db.close();
			return datausage_list;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("all_contact", "" + e);
		}

		return datausage_list;
	}

}
