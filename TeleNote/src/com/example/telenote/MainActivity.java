package com.example.telenote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.telenote.database.DatabaseHandler;
import com.example.telenote.database.ScriptData;
import com.example.telenoterb.R;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Main Activity of TeleNote - TeleNote is a personal assistant that takes note during voice calls for user
 * With the assist of TeleNote, user will no longer need pen/pencil during voice calls
 * 
 * TeleNote is triggered when user press "chatHead" (in service class)
 * TeleNote records voice between two presses
 * Recorded voice is transcribed into text
 * Recorded voice and transcribed text is available to user in IndividualActivity class
 */

public class MainActivity extends Activity {

	ListView lsvMainPage;
	ArrayList<ScriptData> script_data = new ArrayList<ScriptData>();
	DatabaseHandler db;
	Contact_Adapter cAdapter;
	Button btnInfo, btnSetting, btnNextPage, btnPreviousPage, btnUpgrade;
	TextView txtInstructionNumberofPage, txtInstruction, txtQuestionNumber;
	RelativeLayout RL;
	ImageView imgInstruction;
	long[] once = { 0, 50 };
	public static String Settings = "TeleNote Enabled";
	String EnableOrDisable;
	String TranscriptionSetting = "Enable";
	String TranscriptionStatusDisplay;

	private int checkStatus = 0;

	private Boolean firstTime = null;
	private int InstructionPage = 1;
	CallStatus CS = new CallStatus();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// MainActivity.this.deleteDatabase("EachDataManager");
		SimpleDateFormat dfdate = new SimpleDateFormat("MMdd");
		String call_date = dfdate.format(Calendar.getInstance().getTime());
		try{

			db = new DatabaseHandler(this);
			ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();
			String StartDate = Script_data_from_db.get(0).getDate().substring(0, 5).replace("/", "");
			System.out.println("startdate "+StartDate);
			int calculation = Integer.valueOf(call_date) - 
					Integer.valueOf(StartDate);
			
			if(calculation>=7){
				CS.shouldDisable = true;
			}
		}catch(Exception e){
			
		}
		if (!isFirstTime()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			btnInfo = (Button) findViewById(R.id.btnInfo);
			btnSetting = (Button) findViewById(R.id.btnSetting);
			btnUpgrade = (Button) findViewById(R.id.btnUpgrade);
			// mOnTouchListener = new MyTouchListener();
			btnInfo.setOnClickListener(Info);
			btnSetting.setOnClickListener(Setting);
			btnUpgrade.setOnClickListener(Upgrade);

			checkStatus = 0;

			try {
				lsvMainPage = (ListView) findViewById(R.id.lsvMainPage);
				lsvMainPage.setItemsCanFocus(false);
				Set_Referash_Data();
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("some error", "" + e);
			}

		} else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.first_time_launch_main_page);
			// MainActivity.this.deleteDatabase("EachDataManager");
			RL = (RelativeLayout) findViewById(R.id.RL);
			RL.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vb.vibrate(once, -1);
					setInstructionScreen();
				}
			});
		}

	}

	private void setInstructionScreen() {

		setContentView(R.layout.first_time_launch_instruction);
		btnNextPage = (Button) findViewById(R.id.btnNextPage);
		btnPreviousPage = (Button) findViewById(R.id.btnPreviousPage);
		btnNextPage.setOnClickListener(NextPage);
		if (InstructionPage == 1) {
			btnPreviousPage.setEnabled(false);
			;
		} else {
			btnPreviousPage.setEnabled(true);
		}
		if (InstructionPage == 6) {
			btnNextPage.setEnabled(false);
			;
		} else {
			btnNextPage.setEnabled(true);
		}
		btnPreviousPage.setOnClickListener(PreviousPage);
		imgInstruction = (ImageView) findViewById(R.id.imgInstruction);
		txtInstructionNumberofPage = (TextView) findViewById(R.id.txtInstructionNumberofPage);
		txtInstruction = (TextView) findViewById(R.id.txtInstruction);
		txtQuestionNumber = (TextView) findViewById(R.id.txtNumberQuestion);
		txtInstructionNumberofPage.setText(InstructionPage + "/5");
		switch (InstructionPage) {
		case 1:
			imgInstruction.setImageResource(R.drawable.instructionimage1beta);
			txtInstruction.setText(getString(R.string.instruction1));
			txtQuestionNumber.setText(R.string.number_question);
			break;
		case 2:
			imgInstruction.setImageResource(R.drawable.instructionimage2beta);
			txtInstruction.setText(getString(R.string.instruction2));
			break;
		case 3:
			imgInstruction.setImageResource(R.drawable.instructionimage3beta);
			txtInstruction.setText(getString(R.string.instruction3));
			break;
		case 4:
			imgInstruction.setImageResource(R.drawable.instructionimage4beta);
			txtInstruction.setText(getString(R.string.instruction4));
			break;
		case 5:
			TextView TVWarning = (TextView)findViewById(R.id.textView4);
			TVWarning.setText("#基於隱私權,來電筆記只會記錄我方資訊");
			imgInstruction.setImageResource(R.drawable.instructionimage5beta);
			txtInstruction.setText(getString(R.string.instruction5));
			break;
		}
	}

	private boolean isFirstTime() {
		if (firstTime == null) {
			SharedPreferences mPreferences = this.getSharedPreferences(
					"first_time", Context.MODE_PRIVATE);
			firstTime = mPreferences.getBoolean("firstTime", true);
			if (firstTime) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean("firstTime", false);
				editor.commit();
			}
		}
		return firstTime;
	}

	private OnClickListener NextPage = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);
			if (InstructionPage <= 5) {
				InstructionPage = InstructionPage + 1;
				setInstructionScreen();
			}
			if (InstructionPage == 6) {

				try {
					setContentView(R.layout.activity_main);
					// MainActivity.this.deleteDatabase("EachDataManager");
					btnInfo = (Button) findViewById(R.id.btnInfo);
					btnSetting = (Button) findViewById(R.id.btnSetting);
					btnUpgrade = (Button) findViewById(R.id.btnUpgrade);
					// mOnTouchListener = new MyTouchListener();
					btnInfo.setOnClickListener(Info);
					btnSetting.setOnClickListener(Setting);
					btnUpgrade.setOnClickListener(Upgrade);
					lsvMainPage = (ListView) findViewById(R.id.lsvMainPage);
					lsvMainPage.setItemsCanFocus(false);

					Set_Referash_Data();
				} catch (Exception e) {
					// TODO: handle exception
					Log.e("some error", "" + e);
				}
			}

		}
	};

	private OnClickListener PreviousPage = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);
			InstructionPage = InstructionPage - 1;
			setInstructionScreen();
		}
	};

	private OnClickListener Info = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);
			InstructionPage = 1;
			setInstructionScreen();
		}
	};

	private OnClickListener Upgrade = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
			
			alertDialogBuilder.setTitle("升級 來電筆記+");
			
			alertDialogBuilder.setMessage("來電筆記+  直接顯示辨識文字");
			
			LayoutInflater factory = LayoutInflater.from(MainActivity.this);
			final View view = factory.inflate(R.layout.upgrade_alertdialog, null);

			ImageView image= (ImageView) view.findViewById(R.id.imageView);
			image.setImageResource(R.drawable.telenoteupgradeimage);

			alertDialogBuilder.setView(view);


			SimpleDateFormat dfdate = new SimpleDateFormat("dd");
			String Date = dfdate.format(Calendar.getInstance().getTime());
			int INTDate = Integer.valueOf(Date);
			alertDialogBuilder.setPositiveButton("不了，謝謝",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							dialog.cancel();
						}
				
					});
			if (INTDate%2==0) {

				alertDialogBuilder.setNegativeButton(R.string.upgrade30,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								Uri uri = Uri.parse("http://www.telenote-premium.weebly.com");
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								startActivity(intent);							}
						});
			}else{
				alertDialogBuilder.setNegativeButton("好!免費升級  (前往頁面)",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								Uri uri = Uri.parse("http://www.telenote-premium.weebly.com");
								Intent intent = new Intent(Intent.ACTION_VIEW, uri);
								startActivity(intent);							}
						});
			}
			

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
	};

	public String ReturnSettings() {
		return Settings;
	}

	private OnClickListener Setting = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);
			if (Settings.contains("Enable") || Settings.contains("啟用")) {
				EnableOrDisable = getString(R.string.disable_telenote);
			} else if (Settings.contains("Disable") || Settings.contains("停止")) {
				EnableOrDisable = getString(R.string.enable_telenote);
			}
			if (TranscriptionSetting.contains("Enable")) {
				TranscriptionStatusDisplay = "Disable Transcription";
			} else if (TranscriptionSetting.contains("Disable")) {
				TranscriptionStatusDisplay = "Enable Transcription";
			}
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);

			alertDialogBuilder.setTitle(R.string.settings);

			alertDialogBuilder.setPositiveButton(EnableOrDisable,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							if (EnableOrDisable.contains("Disable")
									|| EnableOrDisable.contains("停止")) {
								Settings = getString(R.string.disable_telenote);
							} else if (EnableOrDisable.contains("Enable")
									|| EnableOrDisable.contains("啟用")) {
								Settings = getString(R.string.enable_telenote);
							}
							Toast.makeText(MainActivity.this, Settings, 100)
									.show();
							dialog.cancel();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
	};

	public void Set_Referash_Data() {
		
		if(CS.shouldDisable){
			Toast.makeText(MainActivity.this, "7天試用版已到期", 1000).show();
		}

		checkStatus = 0;

		db = new DatabaseHandler(this);
		ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();

		script_data.clear();
		for (int i = Script_data_from_db.size() - 1; i > -1; i--) {

			if (i == (Script_data_from_db.size() - 1)
					|| !(Script_data_from_db.get(i).getTime()
							.matches(Script_data_from_db.get(i + 1).getTime()))
					|| !(Script_data_from_db.get(i).getDate()
							.matches(Script_data_from_db.get(i + 1).getDate()))
					|| !(Script_data_from_db.get(i).getSecond()
							.matches(Script_data_from_db.get(i + 1).getSecond()))) {
				int id = Script_data_from_db.get(i).getID();
				String PhoneNumber = Script_data_from_db.get(i)
						.getPhoneNumber();
				String Date = Script_data_from_db.get(i).getDate();
				String Time = Script_data_from_db.get(i).getTime();
				String Second = Script_data_from_db.get(i).getSecond();
				String InOutComing = Script_data_from_db.get(i)
						.getInOutComing();

				ScriptData scpt = new ScriptData();
				scpt.setID(id);
				scpt.setPhoneNumber(PhoneNumber);
				scpt.setDate(Date);
				scpt.setTime(Time);
				scpt.setSecond(Second);
				scpt.setInOutComing(InOutComing);
				// cnt.setRecordStart(String.valueOf(RecordStart));
				// cnt.setRecordEnd(String.valueOf(RecordEnd));
				script_data.add(scpt);

			}

		}
		db.close();
		cAdapter = new Contact_Adapter(MainActivity.this,
				R.layout.mainpage_listview, script_data);
		lsvMainPage.setAdapter(cAdapter);
		cAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		checkStatus = 0;
		// Set_Referash_Data();
		super.onResume();
		try {
			Set_Referash_Data();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("some error", "" + e);
		}
		if (!isFirstTime()) {
			Set_Referash_Data();
		}

	}

	public class Contact_Adapter extends ArrayAdapter<ScriptData> {
		Activity activity;
		int layoutResourceId;
		ScriptData user;
		ArrayList<ScriptData> data = new ArrayList<ScriptData>();

		public Contact_Adapter(Activity act, int layoutResourceId,
				ArrayList<ScriptData> data) {
			super(act, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.activity = act;
			this.data = data;
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			View row = convertView;
			UserHolder holder = null;
			ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();

			if (row == null) {
				LayoutInflater inflater = LayoutInflater.from(activity);

				// row = inflater.inflate(layoutResourceId, parent, false);
				row = getLayoutInflater().inflate(R.layout.mainpage_listview,
						null);
				holder = new UserHolder();
				holder.name = (TextView) row.findViewById(R.id.txtNameMpg);
				holder.number = (TextView) row.findViewById(R.id.txtNumberMpg);
				holder.imgMPGfigure = (ImageView) row
						.findViewById(R.id.imgMPGfigure);
				holder.date = (TextView) row.findViewById(R.id.txtDateMpg);
				holder.time = (TextView) row.findViewById(R.id.txtTimeMpg);
				holder.inoutcoming = (ImageView) row
						.findViewById(R.id.imgCallStatusMpg);
				row.setTag(R.layout.mainpage_listview, holder);
			} else {
				holder = (UserHolder) row.getTag(R.layout.mainpage_listview);
			}

			row.setTag(position);
			// row.setOnTouchListener(mOnTouchListener);

			user = data.get(position);

			String CallName = getContactName(user.getPhoneNumber());

			holder.id = user.getID();
			holder.name.setText(CallName);
			holder.number.setText(user.getPhoneNumber());
			holder.date.setText(user.getDate());
			holder.time.setText(user.getTime());
	
			if (user.getInOutComing().contains("out")) {
				holder.inoutcoming
						.setImageResource(R.drawable.telenoteoutgoingcallicon);
			} else if (user.getInOutComing().contains("in")) {
				holder.inoutcoming
						.setImageResource(R.drawable.telenoteincomingcallicon);
			}

			lsvMainPage
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position2, long id) {
							// TODO Auto-generated method stub

							int Position = position2;
							user = data.get(Position);
							int headid = user.getID();
							String name = getContactName(user.getPhoneNumber());
							String number = user.getPhoneNumber();
							String date = user.getDate();
							String time = user.getTime();
							String second = user.getSecond();
							String InOut = user.getInOutComing();
							getCorrectPositionBundleIntent(headid, name,
									number, date, time, second,
									InOut);
						}
					});
			return row;

		}

		class UserHolder {
			TextView name;
			TextView number;
			TextView date;
			TextView time;
			ImageView inoutcoming;
			ImageView imgMPGfigure;
			int id;
			int RecordStart;
			int RecordEnd;
		}

	}

	private String getContactName(String number) {

		String name = null;

		// define the columns I want the query to return
		String[] projection = new String[] {
				ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.PhoneLookup._ID };

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		// query time
		Cursor cursor = MainActivity.this.getContentResolver().query(
				contactUri, projection, null, null, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				name = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
				// Log.v(TAG, "Started uploadcontactphoto: Contact Found @ " +
				// number);
				// Log.v(TAG, "Started uploadcontactphoto: Contact name  = " +
				// name);
			} else {
				// Log.v(TAG, "Contact Not Found @ " + number);
			}
			cursor.close();
		}
		if (name == null) {
			return number;
		}
		return name;
	}

	public void getCorrectPositionBundleIntent(int id, String name,
			String number, String date, String time, String second,
			 String InOut) {

		Intent IndividualIntent = new Intent(MainActivity.this,
				IndividualActivity.class);
		Bundle bd = new Bundle();

		bd.putString("name", name);
		bd.putInt("headID", id);
		// bd.putString("ID", view.getTag().toString());
		bd.putString("number", number);
		bd.putString("date", date);
		bd.putString("time", time);
		bd.putString("inoutcoming", InOut);
		bd.putString("second", second);
		IndividualIntent.putExtras(bd);
		db.close();
		startActivity(IndividualIntent);
	}

}