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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class weightData {
	public weightDataDay allData;
	private weightDataDay ptr;
	private final Context mContext;


	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.getData().getBoolean("error")) {
				Toast.makeText(mContext, "HackDiet: Error saving data!", Toast.LENGTH_LONG).show();
				//} else {
				//    Toast.makeText(mContext, "Successfully saved!", Toast.LENGTH_SHORT).show();
			}
		}
	};

	public weightData() {
		mContext = MonthListActivity.getAppContext();
		allData = new weightDataDay(1970, 1, 1, 0, 0, false, "SPECIAL");
		ptr = allData;
	}

	public void loadData() {
		weightDataDay.autoUpdate = false;
		try {
			FileInputStream fos = mContext.openFileInput("hackdietdata.csv");
			BufferedReader rd = new BufferedReader(new InputStreamReader(fos));
			String line;
			while ((line = rd.readLine()) != null) {
				add(line);
			}
			rd.close();
			fos.close();
		} catch (Exception e) {
			Log.d("LoadThread", e.getMessage());
		}
		weightDataDay.autoUpdate = true;
		allData.setWeight(allData.getWeight()); // now updates the trend of all entries
		//LoadThread t = new LoadThread(handler);
		//t.start();
	}

	public void saveData() {
		SaveThread t = new SaveThread(handler);
		t.start();
	}

	public boolean isLeapYear(int year) {
		if (year % 400 == 0) {
			return true;
		}
		if (year % 100 == 0) {
			return false;
		}
		if (year % 4 == 0) {
			return true;
		}
		return false;
	}

	public int daysInMonth(int month, int year) {
		/*
		 * Yes, this could be written better. I prefer readable.
		 */
		if (month > 7) {
			if (month % 2 == 0) {
				return 31;
			} else {
				return 30;
			}
		} else {
			if (month == 2) {
				if (isLeapYear(year)) {
					return 29;
				} else {
					return 28;
				}
			}
			if (month % 2 == 1) {
				return 31;
			} else {
				return 30;
			}
		}
	}

	public weightDataDay getByDate(int year, int month, int day) {
		weightDataDay retVal = allData;
		while (retVal.year != year || retVal.month != month || retVal.day != day) {
			retVal = retVal.next;
			if (retVal == null) {
				retVal = new weightDataDay(year, month, day, 0.0f, 0, false, "");
				return retVal;
			}
		}
		return retVal;
	}

	public void add(int year, int month, int day, double weight, int rung, boolean flag, String comment) {
		int wholeDate = year * 10000 + month * 100 + day;

		if (ptr.prev == null && ptr.next == null) { // only one entry
			if (ptr.comment.equals("SPECIAL")) { // and even an empty one
				/*if (weight == 0){
					return;
					}*/
				ptr.year = year;
				ptr.month = month;
				ptr.day = day;
				ptr.wholedate = wholeDate;
				ptr.setWeight(weight);
				ptr.rung = rung;
				ptr.flag = flag;
				ptr.comment = comment;
				return;
			}
		}
		if (ptr.wholedate > wholeDate) {
			while (ptr.wholedate > wholeDate && ptr.prev != null) {
				ptr = ptr.prev;
			}
			if (ptr.wholedate > wholeDate) {
				while (ptr.wholedate > wholeDate) {
					int nyear;
					int nmonth;
					int nday;
					int nWholeDate;
					nyear = ptr.year;
					nmonth = ptr.month;
					nday = ptr.day - 1;
					if (nday < 1) {
						nmonth -= 1;
						if (nmonth < 1) {
							nyear -= 1;
							nmonth = 12;
						}
						nday = daysInMonth(nmonth, nyear);
					}
					nWholeDate = nyear * 10000 + nmonth * 100 + nday;

					ptr.prev = new weightDataDay();
					ptr.prev.next = ptr;
					ptr = ptr.prev;
					ptr.day = nday;
					ptr.month = nmonth;
					ptr.year = nyear;
					ptr.setWeight(0);
					ptr.wholedate = nWholeDate;
				}
			}
		}
		if (ptr.wholedate != wholeDate) {
			while (ptr.wholedate < wholeDate && ptr.next != null) {
				ptr = ptr.next;
			}
		}
		if (ptr.wholedate != wholeDate) {
			while (ptr.wholedate < wholeDate) {
				int nyear;
				int nmonth;
				int nday;
				int nwholedate;
				nyear = ptr.year;
				nmonth = ptr.month;
				nday = ptr.day + 1;
				if (nday > daysInMonth(nmonth, nyear)) {
					nday = 1;
					nmonth += 1;
					if (nmonth > 12) {
						nyear += 1;
						nmonth = 1;
					}
				}
				nwholedate = nyear * 10000 + nmonth * 100 + nday;

				ptr.next = new weightDataDay();
				ptr.next.prev = ptr;
				ptr = ptr.next;
				ptr.day = nday;
				ptr.month = nmonth;
				ptr.year = nyear;
				ptr.setWeight(0);
				ptr.wholedate = nwholedate;
			}
		}
		ptr.rung = rung;
		ptr.flag = flag;
		ptr.comment = comment;
		ptr.setWeight(weight);
	}

	public void add(weightDataDay wd) {
		add(wd.year, wd.month, wd.day, wd.getWeight(), wd.rung, wd.flag, wd.comment);
	}

	public void add(String line) {
		//2009-07-01,,,0,
		//2009-07-02,116.3,,0,
		//2012-01-30,110.8,,1,
		//2012-2-1,110.2,34,1,"just a comment"
		String[] elements = line.split(",");
		String[] dateElements = elements[0].split("-");
		int year;
		int month;
		int day;
		try {
			year = Integer.parseInt(dateElements[0]);
			month = Integer.parseInt(dateElements[1]);
			day = Integer.parseInt(dateElements[2]);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
			return;
		}
		if (day > daysInMonth(month, year)) {
			return;
		}
		double weight = Double.parseDouble("0" + elements[1]);
		int rung = Integer.parseInt("0" + elements[2]);
		boolean flag = (elements[3].equalsIgnoreCase("1"));
		String comment = "";
		if (elements.length > 4) {
			if (elements[4].startsWith("\"")) {
				comment = elements[4].substring(1, elements[4].length() - 1);
			} else {
				comment = elements[4];
			}
		}

		add(year, month, day, weight, rung, flag, comment);
	}

	/*
		 private class LoadThread extends Thread {
		 Handler mHandler;

		 LoadThread(Handler h){
		 mHandler = h;
		 }

		 @Override
		 public void run() {
		 try {
		 FileInputStream fos = mContext.openFileInput("hackdietdata.csv");
		 BufferedReader rd = new BufferedReader(new InputStreamReader(fos));
		 String line;
		 while ((line = rd.readLine()) != null){
		 add(line);
		 }
		 rd.close();
		 fos.close();
		 } catch (FileNotFoundException e){ Log.d("LoadThread", e.getMessage());} catch (IOException e) { Log.d("LoadThread", e.getMessage());}
		 Message msg = mHandler.obtainMessage();
		 Bundle b = new Bundle();
		 b.putBoolean("finished", true);
		 msg.setData(b);
		 mHandler.sendMessage(msg);
		 }

		 }
		 */
	private class SaveThread extends Thread {
		Handler mHandler;

		SaveThread(Handler h) {
			mHandler = h;
		}

		@Override
		public void run() {
			try {
				weightDataDay mPtr = allData;
				while (mPtr.prev != null) {
					mPtr = mPtr.prev;
				}
				FileOutputStream fos = mContext.openFileOutput("hackdietdata.csv", Context.MODE_PRIVATE);
				for (; mPtr != null; mPtr = mPtr.next) {
					fos.write(mPtr.toString().getBytes());
					fos.write("\n".getBytes());
				}
				fos.close();
			} catch (Exception e) {
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putBoolean("finished", true);
				b.putBoolean("error", true);
				msg.setData(b);
				mHandler.sendMessage(msg);
				e.printStackTrace();
			}

			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putBoolean("finished", true);
			b.putBoolean("error", false);
			msg.setData(b);
			mHandler.sendMessage(msg);
			//Log.d("SaveThread", "Done saving");
		}
	}
}
