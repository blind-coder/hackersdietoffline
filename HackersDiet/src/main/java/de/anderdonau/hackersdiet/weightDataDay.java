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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class weightDataDay {
	public int year;
	public int month;
	public int day;
	public int wholedate;
	public double weight;
	public int rung;
	public double trend;
	public double var;
	public boolean flag;
	public String comment;
	public weightDataDay next;
	public weightDataDay prev;
	public weightDataDay(){
		Calendar d = new GregorianCalendar();
		day = d.get(Calendar.DAY_OF_MONTH);
		month = d.get(Calendar.MONTH) + 1;
		year = d.get(Calendar.YEAR);
		wholedate = year*10000 + month*100 + day;
		weight = 0.0f;
		rung = 0;
		trend = 0.0f;
		var = 0.0f;
		flag = false;
		comment = "";
		next = null;
		prev = null;
	}
	public weightDataDay(int cyear, int cmonth, int cday, float cweight, int crung, float ctrend, float cvar, boolean cflag, String ccomment){
		day = cday;
		month = cmonth;
		year = cyear;
		wholedate = year*10000 + month*100 + day;
		weight = cweight;
		rung = crung;
		trend = ctrend;
		var = cvar;
		flag = cflag;
		comment = ccomment;
		next = null;
		prev = null;
	}
	public weightDataDay(int cyear, int cmonth, int cday, float cweight, int crung, boolean cflag, String ccomment){
		day = cday;
		month = cmonth;
		year = cyear;
		wholedate = year*10000 + month*100 + day;
		weight = cweight;
		rung = crung;
		trend = 0.0f;
		var = 0.0f;
		flag = cflag;
		comment = ccomment;
		next = null;
		prev = null;
	}
	@Override
	public String toString(){
		String retVal;
		retVal = String.valueOf(year)+"-";
		if (month < 10){
			retVal += "0";
		}
		retVal += String.valueOf(month)+"-";
		if (day < 10){
			retVal += "0";
		}
		retVal += String.valueOf(day);
		retVal += ","+String.valueOf(weight);
		retVal += ","+String.valueOf(rung);
		retVal += ",";
		if (flag){
			retVal += "1";
		} else {
			retVal += "0";
		}
		retVal += ",\""+comment+"\"";
		return retVal;
	}
}
