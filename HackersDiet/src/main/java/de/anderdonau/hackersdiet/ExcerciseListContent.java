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

public class ExcerciseListContent {
	public static List<ExcerciseItem> ITEMS = new ArrayList<ExcerciseItem>();

	public static Map<String, ExcerciseItem> ITEM_MAP = new HashMap<String, ExcerciseItem>();

	public static void addItem(ExcerciseItem item) {
		if (!ITEM_MAP.containsKey(item.id)) {
			ITEMS.add(item);
			ITEM_MAP.put(item.id, item);
		}
	}

	public static class ExcerciseItem {
		public String id;
		public String content;
		public int rung;
		public int month;

		public ExcerciseItem(String id, String content, int rung) {
			this.id = id;
			this.content = content;
			this.rung = rung;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
