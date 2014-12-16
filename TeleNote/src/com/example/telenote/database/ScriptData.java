package com.example.telenote.database;

import android.content.Context;

/*
 * ScriptData keeps track of each record/transcription
 */

public class ScriptData {

	// private variables
	public int _id;
	public String PhoneNumber;
	public String Date;
	public String Time;
	public String Second;
	// InOutComing: 1 = Incoming, 2 = Outcoming
	public String InOutComing;
	public String RecordFile;
	public String TranscriptText;

	DatabaseHandler db;
	Context _context;

	public ScriptData() {

	}

	public ScriptData(int _id, String PhoneNumber, String Date, String Time, String Second,
			String InOutComing, String RecordFile, String TranscriptText) {
		this._id = _id;
		this.PhoneNumber = PhoneNumber;
		this.Date = Date;
		this.Time = Time;
		this.Second = Second;
		this.InOutComing = InOutComing;
		this.RecordFile = RecordFile;
		this.TranscriptText = TranscriptText;
		System.out.println("id is");
		System.out.println(_id);


	}
	

	// getting PhoneNumber
	public String getPhoneNumber() {
		return this.PhoneNumber;
	}

	// setting id
	public int getID() {
		return this._id;
	}

	// getting Date
	public String getDate() {
		return this.Date;
	}

	// getting Time
	public String getTime() {
		return this.Time;
	}
	
	public String getSecond() {
		return this.Second;
	}

	// getting InOutComing
	public String getInOutComing() {
		return this.InOutComing;
	}

	public String getRecordFile() {
		return this.RecordFile;
	}
	
	public String getTransptText() {
		return this.TranscriptText;
	}

	public void setID(int id) {
		this._id = id;
	}

	// setting name
	public void setPhoneNumber(String PhoneNumber) {
		this.PhoneNumber = PhoneNumber;
	}

	// setting phone number
	public void setDate(String Date) {
		this.Date = Date;
	}

	public void setTime(String Time) {
		this.Time = Time;
	}
	
	public void setSecond(String Second) {
		this.Second = Second;
	}

	public void setInOutComing(String InOutComing) {
		this.InOutComing = InOutComing;
	}

	public void setRecordFile(String RecordFile) {
		this.RecordFile = RecordFile;
	}
	
	public void setTranscriptText(String TranscriptText) {
		this.TranscriptText = TranscriptText;
	}

}
