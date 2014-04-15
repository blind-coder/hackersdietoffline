package de.anderdonau.hackersdiet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcerciseListContent {
    public static List<ExcerciseItem> ITEMS = new ArrayList<ExcerciseItem>();

    public static Map<String, ExcerciseItem> ITEM_MAP = new HashMap<String, ExcerciseItem>();

    public static void addItem(ExcerciseItem item) {
        if (!ITEM_MAP.containsKey(item.id)){
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