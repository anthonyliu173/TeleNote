package com.example.telenote.database;

import android.content.Context;

/*
 * UsageData keeps track of data used
 */

public class UsageData {

	public int numberofusagemonth;
	public int numberofusagetotal;
	public int updatemonth;
	public int id;

	DatabaseHandler db;
	Context _context;

	public UsageData() {

	}
	
	// number of total usage : total number of successful service used
	// number of call usage : number of calls that deploy service (note: there may
	// be two or more deployments each call)
	public UsageData(int _id, int numberofusagetotal, int numberofcallusagemonth, int updatemonth){
		
		this.id = _id;
		this.numberofusagemonth = numberofcallusagemonth;
		this.numberofusagetotal = numberofusagetotal;
		this.updatemonth = updatemonth;
		
	}

	public int getNumberOfUsageMonth() {
		return this.numberofusagemonth;
	}

	public int getNumberOfUsageTotal() {
		return this.numberofusagetotal;
	}
	
	public int getID() {
		return this.id;
	}
	
	public int getUpdateMonth() {
		return this.updatemonth;
	}

	public void setUpdateMonth(int updatemonth) {
		this.updatemonth = updatemonth;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void setNumberOfUsageMonth(int numberofusagemonth) {
		this.numberofusagemonth = numberofusagemonth;
	}
	
	public void setNumberOfUsageTotal(int numberofusagetotal) {
		this.numberofusagetotal = numberofusagetotal;
	}
}
