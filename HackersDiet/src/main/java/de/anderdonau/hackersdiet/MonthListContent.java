package de.anderdonau.hackersdiet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.anderdonau.hackersdiet.weightData;
import de.anderdonau.hackersdiet.weightDataDay;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * 
 * TODO: Replace all uses of this class before publishing your app.
 */
public class MonthListContent {
	/**
	 * An array of sample items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

	/**
	 * A map of sample items, by ID.
	 */
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	public static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A item representing a piece of content.
	 */
	public static class DummyItem {
		public String id;
		public String content;
        public int year;
        public int month;

		public DummyItem(String id, String content, int year, int month) {
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
