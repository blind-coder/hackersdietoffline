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
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class weightData {
	public weightDataDay allData;
	private weightDataDay ptr;
	private Context mContext;

/*	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			//Log.d("Handler", "Message received");
		}
	};
*/

	public weightData(){
		mContext = MonthListActivity.getAppContext();
		allData = new weightDataDay(1970, 1, 1, 0, 0, false, "SPECIAL");
		ptr = allData;
	}
	public void loadData() {
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
		//LoadThread t = new LoadThread(handler);
		//t.start();
	}
	public void saveData() {
		try {
			weightDataDay mPtr = allData;
            while (mPtr.prev != null){
                mPtr = mPtr.prev;
            }
			FileOutputStream fos = mContext.openFileOutput("hackdietdata.csv", Context.MODE_PRIVATE);
			for (;mPtr != null; mPtr = mPtr.next){
				fos.write(mPtr.toString().getBytes());
				fos.write("\n".getBytes());
			}
			fos.close();
		} catch (Exception e){
            e.printStackTrace();
        }
		//SaveThread t = new SaveThread(handler);
		//t.start();
	}

	public boolean isleapyear(int year){
		if (year % 400 == 0){
			return true;
		}
		if (year % 100 == 0){
			return false;
		}
		if (year % 4 == 0){
			return true;
		}
		return false;
	}
	public int daysinmonth(int month, int year) {
		if (month > 7){
			if (month % 2 == 0){
				return 31;
			} else {
				return 30;
			}
		} else {
			if (month == 2){
				if (isleapyear(year)){
					return 29;
				} else {
					return 28;
				}
			}
			if (month % 2 == 1){
				return 31;
			} else {
				return 30;
			}
		}
	}
    public weightDataDay getByDate(int year, int month, int day){
        weightDataDay retVal = allData;
        while (retVal.year != year || retVal.month != month || retVal.day != day){
            retVal = retVal.next;
            if (retVal == null){
                retVal = new weightDataDay(year, month, day, 0.0f, 0, false, "");
                return retVal;
            }
        }
        return retVal;
    }
	public void add(int y, int m, int d, String weight, String rung, boolean flag, String comment){
		String retVal = String.valueOf(y)+"-"+String.valueOf(m)+"-"+String.valueOf(d);
		retVal += ","+String.valueOf(weight);
		retVal += ","+String.valueOf(rung);
		if (flag){
			retVal += ",1";
		} else {
			retVal += ",0";
		}
		retVal += ",\""+comment+"\"";
		//Log.d("", retVal);
		add(retVal);
	}
    public void add(weightDataDay wd){
        add(wd.toString());
    }
	public void add(String line){
		//2009-07-01,,,0,
		//2009-07-02,116.3,,0,
		//2012-01-30,110.8,,1,
		//2012-2-1,110.2,34,1,"just a comment"
		String elements[] = line.split(",");
		String dateElements[] = elements[0].split("-");
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
		if (day > daysinmonth(month, year)){
			return;
		}
		int wholedate = year*10000 + month*100 + day;
		double weight = Double.parseDouble("0"+elements[1]);
		int rung = Integer.parseInt("0"+elements[2]);
		boolean flag = (elements[3].equalsIgnoreCase("1"));
		String comment = "";
		if (elements.length > 4){
			if (elements[4].startsWith("\"")){
				comment = elements[4].substring(1, elements[4].length()-1);
			} else {
				comment = elements[4];
			}
		}
		if (ptr.prev == null && ptr.next == null){ // only one entry
			if (ptr.comment.equals("SPECIAL")){ // and even an empty one
				/*if (weight == 0){
					return;
				}*/
				ptr.year = year;
				ptr.month = month;
				ptr.day = day;
				ptr.wholedate = wholedate;
				ptr.weight = weight;
				ptr.rung = rung;
				ptr.trend = weight;
				ptr.var = 0.0f;
				ptr.flag = flag;
				ptr.comment = comment;
				return;
			}
		}
		if (ptr.wholedate > wholedate){
			while (ptr.wholedate > wholedate && ptr.prev != null){
				ptr = ptr.prev;
			}
		}
		if (ptr.wholedate != wholedate){
			while (ptr.wholedate < wholedate && ptr.next != null){
				ptr = ptr.next;
			}
		}
		if (ptr.wholedate != wholedate){
			while (ptr.wholedate < wholedate){
				int nyear; int nmonth; int nday;
				int nwholedate;
				nyear = ptr.year;
				nmonth = ptr.month;
				nday = ptr.day + 1;
				if (nday > daysinmonth(nmonth, nyear)){
					nday = 1;
					nmonth += 1;
					if (nmonth > 12){
						nyear += 1;
						nmonth = 1;
					}
				}
				nwholedate = nyear*10000 + nmonth*100 + nday;

				ptr.next = new weightDataDay();
				ptr.next.prev = ptr;
				ptr = ptr.next;
				ptr.day = nday;
				ptr.month = nmonth;
				ptr.year = nyear;
				ptr.weight = 0;
				ptr.wholedate = nwholedate;
				ptr.trend = ptr.prev.trend;
				ptr.var = 0.0f;
			}
		}
		ptr.rung = rung;
		ptr.flag = flag;
		ptr.comment = comment;
		ptr.weight = weight;
		if (weight == 0){
			if (ptr.prev != null){
				ptr.var = 0.0f;
				ptr.trend = ptr.prev.trend;
				return;
			}
		}
		if (ptr.prev != null){
			ptr.var = ptr.weight - ptr.prev.trend;
			ptr.trend = ptr.prev.trend + (ptr.var / 10);
		} else {
			ptr.var = 0.0f;
			ptr.trend = weight;
		}
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
		 private class SaveThread extends Thread {
		 Handler mHandler;

		 SaveThread(Handler h){
		 mHandler = h;
		 }

		 @Override
		 public void run() {
		 Message msg = mHandler.obtainMessage();
		 Bundle b = new Bundle();
		 b.putBoolean("finished", true);
		 msg.setData(b);
		 mHandler.sendMessage(msg);
//Log.d("SaveThread", "Done saving");
		 }
		 }
		 */
}
