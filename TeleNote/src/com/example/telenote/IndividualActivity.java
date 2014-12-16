package com.example.telenote;

import java.io.IOException;
import java.util.ArrayList;

import com.example.telenote.chathead.Utility;
import com.example.telenote.checkinternet.CheckInternetStatus;
import com.example.telenote.database.DatabaseHandler;
import com.example.telenote.database.ScriptData;
import com.example.telenoterb.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Individual Activity shows information of individual call
 * User is able to:
 * 1. play recorded sound file
 * 2. edit transcribed text
 * 3. dial transcribed number
 * 4. GPS position transcribed address(location)
 */

public class IndividualActivity extends Activity {

	int USER_ID;
	int Duration = 0;
	int LastFocus = -1;
	String Name, Number, Date, Time, inoutcoming, Second;
	int endID;
	int headID;

	int PositionStart;
	TextView txtNameInd, txtNumberInd, txtDateInd, txtTimeInd;
	ImageView imgCallStatusInd;
	Button btnGPS, btnCall, btnExport;

	MediaPlayer m;

	long[] once = { 0, 100 };

	String RecordFilePath;
	String RecordFilePathTrans;
	String TranscriptionText = null;
	String TranscriptionTextChanged = null;

	int position_in_database1;

	// int Position = 0;
	int playPosition;

	int Position;
	
	int NumberOfDataInThisPage;
	boolean shouldAddData;

	DatabaseHandler db;

	DatabaseHandler dbHandler;

	ListView lsvIndPage;
	ArrayList<ScriptData> script_data = new ArrayList<ScriptData>();
	ArrayList<String> TempArray;
	Data_Adapter dAdapter;

	int play = 0;

	int read = 1;

	int PositionChecked = -1;

	CheckInternetStatus cd;

	public void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.individual_call_page);

		NumberOfDataInThisPage = 0;
		shouldAddData = true;
		TempArray = new ArrayList<String>();

		txtNameInd = (TextView) findViewById(R.id.txtNameInd);
		txtNumberInd = (TextView) findViewById(R.id.txtNumberInd);
		txtDateInd = (TextView) findViewById(R.id.txtDateInd);
		txtTimeInd = (TextView) findViewById(R.id.txtTimeInd);
		imgCallStatusInd = (ImageView) findViewById(R.id.imgCallStatusInd);
		btnCall = (Button) findViewById(R.id.btnCall);
		btnGPS = (Button) findViewById(R.id.btnGPS);
		//btnExport = (Button) findViewById(R.id.btnExport);

		btnCall.setOnClickListener(Call);
		btnGPS.setOnClickListener(GPS);
		//btnExport.setOnClickListener(ExportData);

		Bundle bd = this.getIntent().getExtras();
		endID = bd.getInt("headID");
		Name = bd.getString("name");
		Number = bd.getString("number");
		Time = bd.getString("time");
		Date = bd.getString("date");
		inoutcoming = bd.getString("inoutcoming");
		Second = bd.getString("second");

		txtNameInd.setText(Name);
		txtNumberInd.setText(Number);
		txtDateInd.setText(Date);
		txtTimeInd.setText(Time);
		System.out.println("headID is " + String.valueOf(headID));
		LetsGetView();
	}

	public void LetsGetView() {
		// ArrayList<EachData> data = new ArrayList<EachData>();

		if (inoutcoming.contains("out")) {
			imgCallStatusInd
					.setImageResource(R.drawable.telenoteoutgoingcallicon);
		} else if (inoutcoming.contains("in")) {
			imgCallStatusInd
					.setImageResource(R.drawable.telenoteincomingcallicon);
		}

		try {
			script_data.clear();
			db = new DatabaseHandler(this);
			ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();
			System.out.println("here");
			for (int i = 0; i < Script_data_from_db.size(); i++) {
				if (Script_data_from_db.get(i).getSecond().matches(Second)
						&& Script_data_from_db.get(i).getTime().matches(Time)
						&& Script_data_from_db.get(i).getDate().matches(Date)) {

					// if(PositionStart == -1){
					// PositionStart = Script_data_from_db.get(i).getID();
					// }
					TranscriptionText = Script_data_from_db.get(i)
							.getTransptText();
					ScriptData cnt = new ScriptData();
					cnt.setTranscriptText(TranscriptionText);
					if(shouldAddData){
						TempArray.add(TranscriptionText);
					}
					script_data.add(cnt);

				}

			}
			NumberOfDataInThisPage = script_data.size();
			headID = endID - NumberOfDataInThisPage+1;
			shouldAddData = false;
			db.close();
			lsvIndPage = (ListView) findViewById(R.id.lsvIndPage);
			lsvIndPage.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lsvIndPage.setItemsCanFocus(true);
			dAdapter = new Data_Adapter(IndividualActivity.this,
					R.layout.individual_call_listview, script_data);

			lsvIndPage.setAdapter(dAdapter);
			Utility.setListViewHeightBasedOnChildren(lsvIndPage);
			dAdapter.notifyDataSetChanged();
			// Set_Referash_Data();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("some error", "" + e);
		}

	}

	@Override
	public void onBackPressed() {
		// if (TranscriptionTextChanged != null) {
		// Set_Transcription_Refresh_Data(Position, TranscriptionTextChanged);
		// }
		SaveAll();
		// save text
		super.onBackPressed();
	}

	@Override
	public void onPause() {

		SaveAll();

		super.onPause();
	}

	private OnClickListener Call = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (PositionChecked != -1) {
				Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vb.vibrate(once, -1);
				Uri uri = Uri.parse("tel:" + TranscriptionText);
				Intent intent = new Intent(Intent.ACTION_DIAL, uri);
				startActivity(intent);
			}
		}
	};

	private OnClickListener ExportData = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);

			Intent mail_int = new Intent();

			String CombinedText = "";
			for (int i = 0; i < TempArray.size(); i++) {
				if (i > 0) {
					CombinedText = CombinedText + "\n";
				}
				CombinedText = CombinedText + TempArray.get(i);
			}

			mail_int.setAction(android.content.Intent.ACTION_SEND);
			mail_int.setType("text/plain");
			mail_int.putExtra(Intent.EXTRA_SUBJECT, "TeleNote: " + Name);
			mail_int.putExtra(Intent.EXTRA_TEXT, CombinedText);

			startActivity(mail_int);
		}
	};

	private OnClickListener GPS = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (PositionChecked != -1) {
				Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vb.vibrate(once, -1);
				Uri uri = Uri.parse("https://www.google.com.tw/maps/place/"
						+ TranscriptionText + "?z=16");
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
			}
		}
	};

	public void SaveAll() {

		db = new DatabaseHandler(this);
		ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();
		System.out.println(TempArray.size());
		for (int i = 0; i < TempArray.size(); i++) {
			System.out.println(TempArray.get(i));

			String RecFile = Script_data_from_db.get(headID+i).getRecordFile();
			

			db.User_UpDate_Transcription(new ScriptData(headID+i,Number, Date, Time, Second,
					inoutcoming, RecFile ,TempArray.get(i)));


			dAdapter = new Data_Adapter(IndividualActivity.this,
					R.layout.individual_call_listview, script_data);


			lsvIndPage.setAdapter(dAdapter);
			Utility.setListViewHeightBasedOnChildren(lsvIndPage);
			dAdapter.notifyDataSetChanged();

		}
		System.out.println("start save all done");
		db.close();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		LetsGetView();
		super.onResume();
		// Set_Referash_Data();

	}

	public void playRec(int position) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {
		script_data.clear();
		db = new DatabaseHandler(this);
		
		ArrayList<ScriptData> script_data_from_db = db.Get_Scripts();
//		String FilePath = Date+Time+Second+String.valueOf(position+1);
//		FilePath = FilePath.replaceAll("/", "");
//		FilePath = FilePath.replaceAll("-", "");
//		FilePath = FilePath.replaceAll(":", "");
//		String RecFile = null;
//		for(int i = 0; i < script_data_from_db.size(); i++){
//			if(script_data_from_db.get(i).getRecordFile().contains(FilePath)){
//				RecFile = script_data_from_db.get(i).getRecordFile();
//
//			}
//		}
		
		String RecFile = script_data_from_db.get(headID+position).getRecordFile();
		
		if (m == null) {
			m = new MediaPlayer();
			
			try{
				AudioManager am = 
					    (AudioManager) getSystemService(Context.AUDIO_SERVICE);

					am.setStreamVolume(
					    AudioManager.STREAM_MUSIC,
					    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
					    0);
				Toast.makeText(IndividualActivity.this, "播放", 500).show();
				m.setDataSource(RecFile);
				m.prepare();
				m.start();
			}catch(IllegalStateException e){
				
			}
			Duration = m.getDuration();
		} else {
			m.stop();
			m.release();
			m = null;
		}
		db.close();
	}

	public class Data_Adapter extends ArrayAdapter<ScriptData> {
		Activity activity;
		int layoutResourceId;
		ScriptData user;
		ArrayList<ScriptData> data = new ArrayList<ScriptData>();

		public Data_Adapter(Activity act, int layoutResourceId,
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

			if (row == null) {
				LayoutInflater inflater = LayoutInflater.from(activity);

				row = inflater.inflate(layoutResourceId, parent, false);
				holder = new UserHolder();
				holder.layout = (RelativeLayout) row
						.findViewById(R.id.RelativeLayoutIndividualList);
				holder.playstop = (Button) row.findViewById(R.id.btnPlayStop);
				holder.editText = (EditText) row.findViewById(R.id.editText1);
				holder.checkbox = (CheckBox) row.findViewById(R.id.ckbDelete);
				// holder.google = (TextView) row.findViewById(R.id.txtGoogle);
				// if (position == (NumberofData - 1)) {
				// holder.google.setVisibility(View.VISIBLE);
				// }

				if (position == PositionChecked) {
					holder.checkbox.setChecked(true);
				}
				if (position != PositionChecked) {
					holder.checkbox.setChecked(false);
				}
				if (position == PositionChecked) {
					TranscriptionText = data.get(position).getTransptText();
				}

				if (data.get(position).getTransptText().contains("RECORD ONLY")) {
					// holder.editText.setText("Enter Here");
				} else {
					holder.editText
							.setText(data.get(position).getTransptText());
				}

				holder.editText
						.setOnFocusChangeListener(new OnFocusChangeListener() {

							@Override
							public void onFocusChange(View v, boolean hasFocus) {

								if (!hasFocus && position != Position) {
									if (TranscriptionTextChanged != null) {

									}
								}
							}
						});

				holder.playstop.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// if (play ==1 ) {
						Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						vb.vibrate(once, -1);
						if (read == 1) {
							// read = 2;
						}
						try {
							playRec(position);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println(play);
					}

				});

				holder.editText.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						if (s.toString().length() != data.get(position)
								.getTransptText().length()) {
							System.out.println("here");
							TranscriptionTextChanged = s.toString();
							// System.out.println("position is " + position);
							Position = position;
							//TempArray.remove(position);
							TempArray.set(position, s.toString());
							System.out.println("TempArray size is "
									+ TempArray.size() + "text is "+ s.toString());
						}
					}

					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start,
							int before, int count) {

					}
				});
			

				holder.checkbox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						if (cb.isChecked()) {
							PositionChecked = position;
							btnCall.setBackgroundResource(R.drawable.telenotemainpagecallicon);
							btnGPS.setBackgroundResource(R.drawable.telenotemainpagegpsicon);
							SaveAll();
							LetsGetView();
						} else {
							PositionChecked = -1;
							btnCall.setBackgroundResource(R.drawable.telenotemainpagecallicondisable);
							btnGPS.setBackgroundResource(R.drawable.telenotemainpagegpsicondisable);

						}
					}
				});
				row.setTag(holder);

			} else {
				holder = (UserHolder) row.getTag();
			}

			user = data.get(position);
			return row;

		}

		class UserHolder {
			RelativeLayout layout;
			TextView google;
			TextView edText;
			EditText editText;
			Button playstop;
			CheckBox checkbox;
		}

	}
}
