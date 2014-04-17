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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthListContent {
	public static List<MonthItem> ITEMS = new ArrayList<MonthItem>();

	public static Map<String, MonthItem> ITEM_MAP = new HashMap<String, MonthItem>();

	public static void addItem(MonthItem item) {
		if (!ITEM_MAP.containsKey(item.id)){
			ITEMS.add(item);
			ITEM_MAP.put(item.id, item);
		}
	}

	public static class MonthItem {
		public String id;
		public String content;
		public int year;
		public int month;

		public MonthItem(String id, String content, int year, int month) {
			this.id = id;
			this.content = content;
			this.year = year;
			this.month = month;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
