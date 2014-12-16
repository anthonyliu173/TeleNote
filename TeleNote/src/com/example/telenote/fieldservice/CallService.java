package com.example.telenote.fieldservice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.example.telenote.CallStatus;
import com.example.telenote.MainActivity;
import com.example.telenote.chathead.PInfo;
import com.example.telenote.chathead.RetrievePackages;
import com.example.telenote.database.DatabaseHandler;
import com.example.telenote.database.ScriptData;
import com.example.telenoterb.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/*
 * CallService is the core of TeleNote.
 * CallService is called when phone status is changed (incoming call or outgoing call made)
 * CallService pops a chathead
 * User presses chathead when one wants information to be recorded / transcribed
 * CallService is terminated once call is over
 */

public class CallService extends Service implements SensorEventListener {

	double ax;
	double lastx;
	int Count = 0;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mProx;
	private int ProxReading;

	private MediaRecorder myAudioRecorder;
	File fileName;
	private String filename = "record";
	private String filepath = "FileStorage";

	private static final int MIN_DIRECTION_CHANGE = 6;
	private static final int MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = 800;
	private static final int MAX_TOTAL_DURATION_OF_SHAKE = 300;
	private static final int MIN_DURATION_BETWEEN_START_STOP_RECORD = 300;
	private static final int MIN_DURATION_BETWEEN_TWO_RECORD = 500;
	private long mFirstDirectionChangeTime = 0;
	private long mLastDirectionChangeTime = 0;
	private long mDirectionChangeCount = 0;
	private long ShakeRecordStart = 0;
	private long ShakeRecordStop = 0;
	private long CallStartTime = 0;
	int NumberofData = 0;

	// Vibrate once or twice
	long[] once = { 0, 100 };
	long[] twice = { 0, 100, 200, 100 };

	DatabaseHandler db;
	ArrayList<ScriptData> script_data = new ArrayList<ScriptData>();

	String call_number;
	String call_date = null;
	String call_time = null;
	String call_second = null;
	String call_inoutcall = "defult";
	String call_recordstart = null;
	String call_recordend = null;
	int USER_ID;

	private boolean SensorState = false;

	private final int duration = 1; // seconds
	private final int sampleRateTone = 8000;
	private final int numSamples = duration * sampleRateTone / 5;
	private final double sample[] = new double[numSamples];
	private final double freqOfTone1 = 750; // hz
	private final double freqOfTone2 = 650; // hz

	private final byte generatedSnd[] = new byte[2 * numSamples];
	private boolean PlayFirstTime = true;
	private boolean PlayStopRecordTone = false;

	private WindowManager windowManager;
	private ImageView chatHead;

	boolean mHasDoubleClicked = false;
	long lastPressTime;

	ArrayList<String> myArray;
	ArrayList<PInfo> apps;
	List listCity;

	private boolean Touch = false;

	private int SuccessfullDeploymeny = 0;
	private boolean shouldRecord = true;

	CallStatus CS = new CallStatus();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		SimpleDateFormat dfdate = new SimpleDateFormat("MM/dd - yyyy");
		SimpleDateFormat dftime = new SimpleDateFormat("h:mma");
		SimpleDateFormat dfsecond = new SimpleDateFormat("ss");
		call_date = dfdate.format(Calendar.getInstance().getTime());
		call_time = dftime.format(Calendar.getInstance().getTime());
		call_second = dfsecond.format(Calendar.getInstance().getTime());
		System.out.println("second is " + call_second);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		RetrievePackages getInstalledPackages = new RetrievePackages(
				getApplicationContext());
		apps = getInstalledPackages.getInstalledApps(false);
		myArray = new ArrayList<String>();

		for (int i = 0; i < apps.size(); ++i) {
			myArray.add(apps.get(i).appname);
		}

		listCity = new ArrayList();
		for (int i = 0; i < apps.size(); ++i) {
			listCity.add(apps.get(i));
		}

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new ImageView(this);

		chatHead.setImageResource(R.drawable.circlelauncher);

		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 60, getResources()
						.getDisplayMetrics());

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				height, height, WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

		windowManager.addView(chatHead, params);

		try {
			chatHead.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						// Get current time in nano seconds.
						long pressTime = System.currentTimeMillis();

						// If double click...
						if (pressTime - lastPressTime <= 300) {

							// VibrationService.this.stopSelf();
							mHasDoubleClicked = false;
						} else { // If not double click....
							mHasDoubleClicked = false;
						}
						lastPressTime = pressTime;
						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						Touch = true;
						paramsF.x = initialX
								+ (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY
								+ (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(chatHead, paramsF);
						break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

		chatHead.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (shouldRecord) {
					if (Touch) {
						Touch = false;
						System.out.println(String.valueOf(Touch));
					} else if (Count == 0) {
						System.out.println("false " + String.valueOf(Touch));
						
						startRecording();
						chatHead.setImageResource(R.drawable.circlelauncherrecording);
						Count = 1;
					} else if (Count == 1) {
						chatHead.setImageResource(R.drawable.circlelauncher);
						stopRecording();
						Count = 0;
					}
				} else {
					Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vb.vibrate(once, -1);
					Toast.makeText(CallService.this,
							"Please upgrade to premium", 750).show();
				}
			}
		});

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		super.onStartCommand(intent, flags, startId);
		CallStartTime = System.currentTimeMillis();
		NumberofData = 0;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		mProx = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mSensorManager.registerListener(this, mProx,
				SensorManager.SENSOR_DELAY_NORMAL);
		ProxReading = 5;
		mFirstDirectionChangeTime = 0;
		mLastDirectionChangeTime = 0;
		mDirectionChangeCount = 0;
		ShakeRecordStart = 0;
		ShakeRecordStop = 0;
		Count = 0;

		SensorState = true;

		call_number = (String) intent.getExtras().get("CallNumber");
		call_inoutcall = (String) intent.getExtras().get("In-N-Out");

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {

		if (chatHead != null)
			windowManager.removeView(chatHead);

		SensorState = false;
		mSensorManager.unregisterListener(CallService.this);

		if (Count == 1) {
			stopRecording();
		}

		if (NumberofData != 0) {
			Notify("來電筆記", NumberofData + " 項記錄 ");
		}

		super.onDestroy();

	}

	private void Notify(String notificationTitle, String notificationMessage) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(
				R.drawable.telenotenotificationicon, "New Message",
				System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(CallService.this,
				notificationTitle, notificationMessage, pendingIntent);
		notificationManager.notify(9999, notification);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor source = event.sensor;

		if (ProxReading < 2) {

			if (source.equals(mAccelerometer)) {
				// do your stuff
				ax = event.values[0];

				double totalMovement = Math.pow(ax, 2);
				if (totalMovement > 225) {
					long now = System.currentTimeMillis();

					if (mFirstDirectionChangeTime == 0) {
						mFirstDirectionChangeTime = now;
						mLastDirectionChangeTime = now;
					}

					long lastChangeWasAgo = now - mLastDirectionChangeTime;
					if (lastChangeWasAgo < MAX_PAUSE_BETWEEN_DIRECTION_CHANGE) {
						mLastDirectionChangeTime = now;
						mDirectionChangeCount++;

						lastx = ax;

						if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE) {
							long totalDuration = now
									- mFirstDirectionChangeTime;
							if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {

								if (Count == 0) {
									ShakeRecordStart = now;
									if (ShakeRecordStart - ShakeRecordStop >= MIN_DURATION_BETWEEN_TWO_RECORD) {

										long StartTime = System
												.currentTimeMillis();
										if (StartTime - CallStartTime > 100) {
											
											if (shouldRecord) {
												startRecording();
												Count = 1;
											}
										}
									}
								} else if (Count == 1) {
									ShakeRecordStop = now;
									if (ShakeRecordStop - ShakeRecordStart >= MIN_DURATION_BETWEEN_START_STOP_RECORD) {
										stopRecording();
									}
								}
								resetShakeParameters();
							}
						}
					} else {
						resetShakeParameters();
					}
				}
			}
		}
		if (source.equals(mProx)) {
			ProxReading = (int) event.values[0];
		}
	}

	private void startRecording() {
		if (CS.shouldDisable) {
			Toast.makeText(CallService.this, "來電筆記: 7天試用版已到期", 1000).show();
		} else {
			NumberofData = NumberofData + 1;
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(once, -1);

			PlayFirstTime = true;
			PlayStopRecordTone = false;
			genTone(freqOfTone1);
			playSound();

			myAudioRecorder = new MediaRecorder();
			myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			myAudioRecorder
					.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT);
			// possible rate 44100
			myAudioRecorder.setAudioSamplingRate(44100);

			String RecordFilePathTime = call_date + call_time + call_second;
			RecordFilePathTime = RecordFilePathTime.replaceAll("/", "");
			RecordFilePathTime = RecordFilePathTime.replaceAll("-", "");
			RecordFilePathTime = RecordFilePathTime.replaceAll(":", "");

			ContextWrapper contextWrapper = new ContextWrapper(
					getApplicationContext());
			System.out.println("started1");
			File directory = contextWrapper.getDir(filepath,
					Context.MODE_PRIVATE);
			System.out.println("started2");
			String FloatFilename = filename + RecordFilePathTime
					+ Integer.toString(NumberofData);
			System.out.println("started3");
			fileName = new File(directory, FloatFilename);
			System.out.println("started4");
			myAudioRecorder.setOutputFile(fileName.getAbsolutePath());

			try {
				myAudioRecorder.prepare();
				myAudioRecorder.start();

			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void stopRecording() {
		if (CS.shouldDisable) {
			Toast.makeText(CallService.this, "7天試用版已到期", 1000).show();
		} else {
			Count = 0;
			Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vb.vibrate(twice, -1);
			PlayStopRecordTone = true;
			PlayFirstTime = true;
			System.out.println("here");
			myAudioRecorder.stop();
			System.out.println("here1");
			myAudioRecorder.release();
			System.out.println("here2");
			myAudioRecorder = null;
			System.out.println("here3");
			genTone(freqOfTone2);
			playSound();

			db = new DatabaseHandler(this);

			ArrayList<ScriptData> Script_data_from_db = db.Get_Scripts();
			if (Script_data_from_db.size() == 0) {
				USER_ID = 0;
			} else {
				USER_ID = Script_data_from_db.get(
						Script_data_from_db.size() - 1).getID() + 1;
			}
			db.Add_Contact(new ScriptData(USER_ID, call_number, call_date,
					call_time, call_second, call_inoutcall, fileName
							.getAbsolutePath(), "Enter here"));
			db.close();

			Toast.makeText(CallService.this, "stop recording", 100).show();
		}
	}

	private void resetShakeParameters() {
		mFirstDirectionChangeTime = 0;
		mDirectionChangeCount = 0;
		mLastDirectionChangeTime = 0;
		lastx = 0;

	}

	public void genTone(double freqOfTone) {

		for (int i = 0; i < numSamples; ++i) {
			sample[i] = Math.sin(2 * Math.PI * i
					/ (sampleRateTone / freqOfTone));
		}

		int idx = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	public void playSound() {
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				sampleRateTone, AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
				AudioTrack.MODE_STATIC);
		float volume = (float) 0.5;
		audioTrack.setStereoVolume(volume, volume);
		audioTrack.write(generatedSnd, 0, generatedSnd.length);

		audioTrack.play();
	}

}