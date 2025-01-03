package de.anderdonau.hackersdiet;
/*
	 The Hackers Diet Offline for Android
	 Copyright (C) 2014 Benjamin Schieder <hackersdiet@wegwerf.anderdonau.de>

	 This program is free software; you can redistribute it and/or modify
	 it under the terms of the GNU General Public License as published by
	 the Free Software Foundation; either version 2 of the License.

	 This program is distributed in the hope that it will be useful,
	 but WITHOUT ANY WARRANTY; without even the implied warranty of
	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 GNU General Public License for more details.

	 You should have received a copy of the GNU General Public License along
	 with this program; if not, write to the Free Software Foundation, Inc.,
	 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
	 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Prefs extends Activity {
	ProgressDialog pBar = null;
	Context mContext = this;

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int id;
			boolean finished;
			id = msg.getData().getInt("sendToast", -1);
			finished = msg.getData().getBoolean("finished", false);
			if (finished) {
				if (pBar != null) {
					if (pBar.isShowing()) {
						pBar.dismiss();
					}
				}
				pBar = null;
			} else if (id >= 0) {
				Toast.makeText(mContext, id, Toast.LENGTH_SHORT).show();
			}
		}
	};

	public void loadData(String username, String password) {
		pBar = ProgressDialog.show(this, "", getResources().getString(R.string.downloadingFromHackDietOnline), true);
		LoadThread t = new LoadThread(handler, username, password);
		t.start();
	}

	public void saveData(String username, String password) {
		pBar = ProgressDialog.show(this, "", getResources().getString(R.string.uploadingToHackDietOnline), true);
		SaveThread t = new SaveThread(handler, username, password);
		t.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefs);
		SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);

		String username = settings.getString("username", "");
		assert username != null;
		if (!username.isEmpty()) {
			EditText u = findViewById(R.id.textUsername);
			u.setText(username);
		}

		String password = settings.getString("password", "");
		assert password != null;
		if (!password.isEmpty()) {
			EditText p = findViewById(R.id.textPassword);
			p.setText(password);
		}

		boolean autosave = settings.getBoolean("autosave", true);
		CheckBox btnAutoSave = findViewById(R.id.btnAutoSave);
		btnAutoSave.setChecked(autosave);
	}

	public void downloadDataFromHackDietOnline() {
		SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);

		final String username = settings.getString("username", "");
		assert username != null;
		if (username.isEmpty()) {
			Toast.makeText(this, R.string.errorUsernameIsEmpty, Toast.LENGTH_LONG).show();
			return;
		}

		final String password = settings.getString("password", "");
		assert password != null;
		if (password.isEmpty()) {
			Toast.makeText(this, R.string.errorPasswordIsEmpty, Toast.LENGTH_LONG).show();
			return;
		}

		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setMessage(R.string.reallySyncFromHDO).setCancelable(false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				loadData(username, password);
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = confirm.create();
		alert.show();
	}

	public void uploadDataToHackDietOnline() {
		SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);

		final String username = settings.getString("username", "");
		assert username != null;
		if (username.isEmpty()) {
			Toast.makeText(this, R.string.errorUsernameIsEmpty, Toast.LENGTH_LONG).show();
			return;
		}

		final String password = settings.getString("password", "");
		assert password != null;
		if (password.isEmpty()) {
			Toast.makeText(this, R.string.errorPasswordIsEmpty, Toast.LENGTH_LONG).show();
			return;
		}

		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setMessage(R.string.reallySyncToHDO).setCancelable(false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				saveData(username, password);
				dialog.dismiss();
			}
		}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = confirm.create();
		alert.show();
	}

	public void buttonSyncFromHDOnline(View view) {
		savePrefs();
		downloadDataFromHackDietOnline();
	}

	public void buttonSyncToHDOnline(View view) {
		savePrefs();
		uploadDataToHackDietOnline();
	}

	public void buttonAutoSave(View view) {
		SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);
		CheckBox btnAutoSave = (CheckBox) view;
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("autosave", btnAutoSave.isChecked());
		editor.apply();
	}

	public void savePrefs() {
		SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);
		SharedPreferences.Editor editor = settings.edit();
		EditText u = (EditText) findViewById(R.id.textUsername);
		EditText p = (EditText) findViewById(R.id.textPassword);
		editor.putString("username", u.getText().toString());
		editor.putString("password", p.getText().toString());
		editor.apply();
	}

	@Override
	public void onBackPressed() {
		savePrefs();
		finish();
	}

	private class HttpThread extends Thread {
		public Handler mHandler;
		public String mUsername;
		public String mPassword;
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				/*
				 * TODO: Stop being so lazy.
				 */
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		HttpThread(Handler handle, String username, String password) {
			mHandler = handler;
			mUsername = username;
			mPassword = password;
			try {
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		}

		public String getHackDietOnlineSession(String username, String password) {
			sendToast(R.string.loginInProgress);
			try {
				String rawData = "HDiet_username=" + URLEncoder.encode(username, "UTF-8");
				rawData += "&HDiet_password=" + URLEncoder.encode(password, "UTF-8");
				rawData += "&q=validate_user";
				rawData += "&login=" + URLEncoder.encode(" Sign In ", "UTF-8");

				HttpsURLConnection conn = POST(rawData);

				Log.d("retcode", "Return code: " + conn.getResponseCode());
				// Get Response
				InputStreamReader isr = new InputStreamReader(conn.getInputStream());
				BufferedReader rd = new BufferedReader(isr);

				String line;
				while ((line = rd.readLine()) != null) {
					if (line.contains("name=\"s\"")) {
						Pattern r = Pattern.compile("value=\"(.*)\"");
						Matcher m = r.matcher(line);
						if (m.find()) {
							String session = m.group(1);
							rd.close();
							conn.disconnect();
							sendToast(R.string.loginSuccessful);
							return session;
						}
					}
				}
				rd.close();
				conn.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			sendToast(R.string.errorLoginFailed);
			return "";
		}

		public void sendToast(int id) {
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("sendToast", id);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

		public HttpsURLConnection POST(String data) {
			try {
				// Open Connection
				URL url = new URL("https://www.fourmilab.ch/cgi-bin/HackDiet");
				HttpsURLConnection conn;
				OutputStreamWriter wr;
				conn = (HttpsURLConnection) url.openConnection();
				conn.setDoOutput(true);
				wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();
				return conn;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class LoadThread extends HttpThread {
		public LoadThread(Handler handle, String username, String password) {
			super(handle, username, password);
		}

		@Override
		public void run() {
			String session = getHackDietOnlineSession(mUsername, mPassword);
			if (!session.isEmpty()) {
				String data = getHackDietOnlineData(session);
				if (data.length() >= 0) {
					try {
						FileOutputStream fos = openFileOutput("hackdietdata.csv", Context.MODE_PRIVATE);
						fos.write(data.getBytes());
						fos.close();
						MonthListActivity.mWeightData.loadData();
					} catch (Exception e) {
						e.printStackTrace();
					}
					sendToast(R.string.downloadFromHDOSuccessful);
				}
			}

			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("finished", true);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

		public String getHackDietOnlineData(String session) {
			sendToast(R.string.requestingDataFromHDO);
			try {
				String rawData = "format=palm&period=a&s=" + session + "&q=do_exportdb";

				// Open Connection
				HttpsURLConnection conn = POST(rawData);

				// Get Response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				StringBuilder retVal;
				retVal = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					retVal.append(line).append("\n");
				}
				rd.close();
				conn.disconnect();
				return retVal.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendToast(R.string.errorCouldNotReadData);
			return "";
		}
	}

	private class SaveThread extends HttpThread {
		public SaveThread(Handler handle, String username, String password) {
			super(handle, username, password);
		}

		@Override
		public void run() {
			String session = getHackDietOnlineSession(mUsername, mPassword);
			if (session.length() <= 0) {
				return;
			}

			if (sendHackDietOnlineData(session)) {
				sendToast(R.string.uploadToHDOSuccessful);
			}

			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("finished", true);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

		public boolean sendHackDietOnlineData(String session) {
			String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
			StringBuilder POSTDATA = new StringBuilder();
			sendToast(R.string.sendigDataToHDO);
			try {
				POSTDATA.append("--").append(boundary).append("\n");
				POSTDATA.append("Content-Disposition: form-data; name=\"s\"\n\n").append(session).append("\n");
				POSTDATA.append("--").append(boundary).append("\n");
				POSTDATA.append("Content-Disposition: form-data; name=\"q\"\n\ncsv_import_data\n");
				POSTDATA.append("--").append(boundary).append("\n");
				POSTDATA.append("Content-Disposition: form-data; name=\"overwrite\"\n\ny\n");
				POSTDATA.append("--").append(boundary).append("\n");
				POSTDATA.append("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"data.csv\"\n");
				POSTDATA.append("Content-Type: text/csv\n\n");
				POSTDATA.append("Date,Weight,Rung,Flag,Comment\n");
				POSTDATA.append("StartTrend,0.0000,0,0,0\n");
				FileInputStream fin = mContext.openFileInput("hackdietdata.csv");
				BufferedReader rd = new BufferedReader(new InputStreamReader(fin));
				String line;
				while ((line = rd.readLine()) != null) {
					POSTDATA.append(line).append("\n");
				}
				rd.close();
				fin.close();
				POSTDATA.append("\n--").append(boundary).append("--\n");
				String rawData = "q=csv_import_data&overwrite=y&s=" + session + "&file=" + URLEncoder.encode(POSTDATA.toString(), "UTF-8");

				HttpsURLConnection conn = POST(rawData);

				int r = conn.getResponseCode();
				if (r < 300 && r > 199) {
					POST("s=" + session + "&q=update_trend&m=0000-00&canon=0");
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendToast(R.string.errorCouldNotReadData);
			return false;
		}
	}
}
