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
import java.util.Locale;

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
	public weightDataDay(int year, int month, int day, float weight, int rung, float trend, float var, boolean flag, String comment){
		this.day = day;
		this.month = month;
		this.year = year;
		this.wholedate = year*10000 + month*100 + day;
		this.weight = weight;
		this.rung = rung;
		this.trend = trend;
		this.var = var;
		this.flag = flag;
		this.comment = comment;
		this.next = null;
		this.prev = null;
	}
	public weightDataDay(int year, int month, int day, float weight, int rung, boolean flag, String comment){
		this.day = day;
		this.month = month;
		this.year = year;
		this.wholedate = year*10000 + month*100 + day;
		this.weight = weight;
		this.rung = rung;
		this.trend = 0.0f;
		this.var = 0.0f;
		this.flag = flag;
		this.comment = comment;
		this.next = null;
		this.prev = null;
	}
	@Override
	public String toString(){
		String retVal;
		retVal = String.format(Locale.ENGLISH, "%d-%02d-%02d,%f,%d,%d,\"%s\"", year, month, day, weight, rung, flag ? 1 : 0, comment);
		return retVal;
	}
}
