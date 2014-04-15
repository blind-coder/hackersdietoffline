package de.anderdonau.hackersdiet;

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