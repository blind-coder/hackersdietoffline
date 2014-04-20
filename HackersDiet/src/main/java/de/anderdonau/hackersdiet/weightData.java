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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.*;

public class weightData {
    public List<weightDataDay> lstWeightData = new ArrayList<weightDataDay>();
	private Context mContext;
    public boolean autoUpdate = true;
    private boolean debugGetByDate = false;

	/**
	 * final Handler handler = new Handler() {
	 * public void handleMessage(Message msg) {
	 * //Log.d("Handler", "Message received");
	 * }
	 * };
	 */

	public weightData(){
		mContext = MonthListActivity.getAppContext();
	}
	public void loadData() {
        autoUpdate = false;
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
        sort(lstWeightData);
        autoUpdate = true;
        recalculateTrend();
		//LoadThread t = new LoadThread(handler);
		//t.start();
	}
	public void saveData() {
		try {
            sort(lstWeightData);
            Iterator<weightDataDay> it = lstWeightData.iterator();
            FileOutputStream fos = mContext.openFileOutput("hackdietdata.csv", Context.MODE_PRIVATE);

			while (it.hasNext()){
                weightDataDay p = it.next();
				fos.write(p.toString().getBytes());
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
		/**
		 * Yes, this could be written better. I prefer readable.
		 */
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
        Iterator<weightDataDay> it = lstWeightData.iterator();
        weightDataDay p;
        if (debugGetByDate)
            Log.d("getByDate", String.format("Searching %d-%02d-%02d", year, month, day));
        while (it.hasNext()){
            p = it.next();
            if (p.year == year && p.month == month && p.day == day){
                return p;
            }
            if (debugGetByDate)
                Log.d("getByDate", String.format("%d-%02d-%02d != %d-%02d-%02d", p.year, p.month, p.day, year, month, day));
        }
        return null;
	}
	public void add(int y, int m, int d, String weight, String rung, boolean flag, String comment){
		String retVal = String.valueOf(y)+"-"+String.valueOf(m)+"-"+String.valueOf(d);
		retVal += ","+String.valueOf(weight);
		retVal += ","+rung;
		if (flag){
			retVal += ",1";
		} else {
			retVal += ",0";
		}
		retVal += ",\""+comment+"\"";
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
		double weight;
        try {
            weight = Double.parseDouble(elements[1]);
        } catch (Exception e) {
            weight = 0;
        }
        if (weight == 0){
            weight = Double.NaN;
        }
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

        weightDataDay p;
        debugGetByDate = true;
        if ((p = getByDate(year, month, day)) != null) {
            Log.d("add", String.format("getByDate returned an entry for %d-%02d-%02d!", year, month, day));
            p.weight = weight;
            p.rung = rung;
            p.trend = weight;
            p.var = 0.0f;
            p.flag = flag;
            p.comment = comment;
        } else {
            Log.d("add", String.format("getByDate did not return %d-%02d-%02d", year, month, day));
            p = new weightDataDay();
            p.year = year;
            p.month = month;
            p.day = day;
            p.wholedate = wholedate;
            p.weight = weight;
            p.rung = rung;
            p.trend = weight;
            p.var = 0.0f;
            p.flag = flag;
            p.comment = comment;
            lstWeightData.add(p);
        }
        debugGetByDate = false;

        weightDataDay prev = p;

        prev.day--;
        if (prev.day < 1){
            prev.month--;
            if (prev.month < 1){
                prev.year--;
                prev.month = 12;
            }
            prev.day = daysinmonth(prev.year, prev.month);
        }

        prev = getByDate(prev.year, prev.month, prev.day);
        if (prev != null){
            p.var = p.weight - prev.trend;
            p.trend = prev.trend + (p.var / 10);
        }

        if (autoUpdate){
            this.recalculateTrend();
        }
	}

    public void recalculateTrend(){
        weightDataDay prev = new weightDataDay();
        prev.trend = Double.NaN;
        Collections.sort(lstWeightData);
        for (weightDataDay cur : lstWeightData) {
            if (Double.isNaN(prev.trend)){
                cur.var = 0.0;
                prev.trend = cur.trend = cur.weight;
            } else {
                if (Double.isNaN(cur.weight)){
                    cur.var = 0.0;
                    cur.trend = prev.trend;
                } else {
                    cur.var = cur.weight - prev.trend;
                    cur.trend = prev.trend + (cur.var / 10);
                    prev.trend = cur.trend;
                }
            }
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
