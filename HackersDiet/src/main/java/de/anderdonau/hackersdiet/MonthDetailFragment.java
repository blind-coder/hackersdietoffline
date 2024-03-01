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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.*;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Month detail screen.
 * This fragment is either contained in a {@link MonthListActivity}
 * in two-pane mode (on tablets) or a {@link MonthDetailActivity}
 * on handsets.
 */
public class MonthDetailFragment extends Fragment {
	Calendar mToday;
	View rootView = null;
	boolean mCanSave = false;
	boolean viewCachePopulated = false;
	public static final String ARG_ITEM_ID = "item_id";
	private MonthListContent.MonthItem mItem;
	viewCache[] mViewCache = new viewCache[32]; // 0 is left empty, saves subtracting 1 every time
	weightData mWeight = null;
	weightDataDay mPtr = null;
	GraphView graphView;

	/**
	 * Search a widget by its name and return its ID.
	 */
	public int getIdByName(String name) {
		Class res = R.id.class;
		int id;
		try {
			Field field = res.getField(name);
			id = field.getInt(null);
		} catch (Exception e) {
			Log.d("getIdByName", "Cant get " + name);
			return -1;
		}
		return id;
	}

	/**
	 * Main drawing-handling function. Updates all widgets and text
	 */
	public void updateEverything() {
		mCanSave = false; // while this is running, we prevent saving. Works as a mutex.

		for (int d = 1; d <= 31; d++) {
			// if (d > 28 && d > mWeight.daysinmonth(mToday.get(Calendar.MONTH) + 1, mToday.get(Calendar.YEAR))) {
			if (d > 28 && d > mWeight.daysinmonth(mItem.month, mItem.year)) {
				/**
				 * Hide all widgets that are not used in this month and set them to empty strings or false.
				 */
				mViewCache[d].row.setVisibility(View.GONE);
				mViewCache[d].weight.setText("");
				mViewCache[d].trend.setText(" ");
				mViewCache[d].var.setText(" ");
				mViewCache[d].rung.setText("");
				mViewCache[d].flag.setChecked(false);
				mViewCache[d].comment.setText("");
			} else if (d > 28) {
				/**
				 * Obviously only necessary for days 29 and 30 for feb, and 31 for apr, jun, sep, nov.
				 */
				mViewCache[d].row.setVisibility(View.VISIBLE);
			}
			if (d <= mWeight.daysinmonth(mToday.get(Calendar.MONTH) + 1, mToday.get(Calendar.YEAR))) {
				mToday.set(Calendar.DAY_OF_MONTH, d);
			}
		}

		int year = mItem.year;
		int month = mItem.month;
		int wholedate = year * 10000 + month * 100 + 1;
		int wholedatelast = year * 10000 + month * 100 + mWeight.daysinmonth(month, year);
		Calendar tmpDate = new GregorianCalendar(year, month - 1, 1);
		double max = -1;
		double min = 99999;

		SimpleDateFormat sdfDay = new SimpleDateFormat("MMMM yyyy");
		((TextView) rootView.findViewById(R.id.textMonth)).setText(sdfDay.format(tmpDate.getTime()));

		mPtr = mWeight.allData;

		if (mPtr.wholedate < wholedate) { // mPtr is before today
			while (mPtr.wholedate < wholedate && mPtr.next != null) {
				mPtr = mPtr.next;
			}
			if (mPtr.wholedate < wholedate) { // No entries for this month
				mCanSave = true;
				return;
			}
		} else if (mPtr.wholedate > wholedate) { // mPtr is beyond today
			while (mPtr.wholedate > wholedate && mPtr.prev != null) {
				mPtr = mPtr.prev;
			}
			if (mPtr.wholedate > wholedatelast) { // No entries for this month
				mCanSave = true;
				return;
			}
		}

		int numWeight = 0;
		int numRung = 0;
		LineGraphSeries<DataPoint> weightValues = new LineGraphSeries<DataPoint>();
		LineGraphSeries<DataPoint> trendValues = new LineGraphSeries<DataPoint>();
		LineGraphSeries<DataPoint> rungValues = new LineGraphSeries<DataPoint>();
		weightValues.setTitle("Weight");
		trendValues.setTitle("Trend");
		rungValues.setTitle("Rung");
		weightValues.setColor(Color.GREEN);
		trendValues.setColor(Color.RED);
		rungValues.setColor(Color.BLUE);
		weightValues.setDrawDataPoints(true);
		trendValues.setDrawDataPoints(true);
		rungValues.setDrawDataPoints(true);
		for (; mPtr != null && mPtr.wholedate <= wholedatelast; mPtr = mPtr.next) { // fill all values
			int d = mPtr.wholedate % (year * 10000 + month * 100);
			max = Math.max(max, mPtr.getTrend());
			min = Math.min(min, mPtr.getTrend());
			if (mPtr.getWeight() > 0.0f) { // only care if we have an actual value
				max = Math.max(max, mPtr.getWeight());
				min = Math.min(min, mPtr.getWeight());
				weightValues.appendData(new DataPoint(d, mPtr.getWeight()), false, 31);
				trendValues.appendData(new DataPoint(d, mPtr.getTrend()), false, 31);
			} else {
				weightValues.appendData(new DataPoint(d, Double.NaN), false, 31);
				trendValues.appendData(new DataPoint(d, Double.NaN), false, 31);
			}
			numWeight++;

			if (mPtr.rung > 0) {
				rungValues.appendData(new DataPoint(d, mPtr.rung), false, 31);
				numRung++;
			}

			if (mPtr.getVar() > 0) {
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightGain));
			} else if (mPtr.getVar() < 0) {
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightLoss));
			} else {
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightConstant));
			}

			mViewCache[d].weight.setText(String.valueOf(mPtr.getWeight()));
			mViewCache[d].trend.setText(String.format("%.1f", mPtr.getTrend()));
			mViewCache[d].var.setText(String.format("%+.1f", mPtr.getVar()));
			mViewCache[d].rung.setText(String.valueOf(mPtr.rung));
			mViewCache[d].flag.setChecked(mPtr.flag);
			mViewCache[d].comment.setText(mPtr.comment);

			tmpDate.set(Calendar.DAY_OF_MONTH, d);
		}
		for (int i=numWeight+1; i<=31; i++){
			weightValues.appendData(new DataPoint(i, Double.NaN), false, 31);
			trendValues.appendData(new DataPoint(i, Double.NaN), false, 31);
			rungValues.appendData(new DataPoint(i, Double.NaN), false, 31);
		}
		graphView = rootView.findViewById(R.id.weightGraph);
		graphView.removeAllSeries();
		graphView.addSeries(weightValues);
		graphView.addSeries(trendValues);
		if (numRung > 0){
			graphView.addSeries(rungValues);
		}
		graphView.getLegendRenderer().setVisible(true);
		if (mPtr != null) {
			graphView.getLegendRenderer().setAlign(mPtr.getWeight() < (max - (max - min) / 2) ? LegendRenderer.LegendAlign.TOP : LegendRenderer.LegendAlign.BOTTOM);
		}
		graphView.getViewport().setXAxisBoundsManual(true);
		graphView.getViewport().setMinX(0);
		graphView.getViewport().setMaxX(numWeight);
		GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();
		gridLabelRenderer.setHumanRounding(true, false);
		NumberFormat nfy = NumberFormat.getInstance();
		nfy.setMinimumFractionDigits(1);
		nfy.setMaximumFractionDigits(1);
		nfy.setMinimumIntegerDigits(1);
		NumberFormat nfx = NumberFormat.getInstance();
		nfx.setMinimumFractionDigits(0);
		nfx.setMaximumFractionDigits(0);
		nfx.setMinimumIntegerDigits(1);
		gridLabelRenderer.setLabelFormatter(new DefaultLabelFormatter(nfx, nfy));
		//graphView.getViewport().setMinX(1);
		graphView.getViewport().setMaxX(31);
		graphView.getViewport().setMinY(min);
		graphView.getViewport().setMaxY(max);
		/**
		 * TODO: Need a second Y-Axis
		 * if (numRung > 0){
		 *   GraphView.GraphViewData[] rungValues = new GraphView.GraphViewData[numRung];
		 *   System.arraycopy(tmpRungValues, 0, rungValues, 0, numRung);
		 *   graphView.addSeries(new GraphViewSeries("Rung", null, rungValues));
		 * }
		 */
		mCanSave = true;
	}

	public MonthDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = MonthListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		} else {
			return;
		}

		mToday = new GregorianCalendar();
		mWeight = MonthListActivity.getmWeightData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_month_detail, container, false);

		mPtr = mWeight.allData;

		View.OnFocusChangeListener onBlur = new View.OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean hasFocus) {
				if (hasFocus) { // only update on loss of focus
					return;
				}
				if (MonthListActivity.mChanged) {
					updateEverything();
				}
			}
		};

		if (!viewCachePopulated) {
			for (int d = 1; d <= 31; d++) {
				String day = "";
				if (d < 10) {
					day = "0";
				}
				day += String.valueOf(d);
				mViewCache[d] = new viewCache();
				mViewCache[d].weight = (EditText) rootView.findViewById(getIdByName("weight" + day));
				mViewCache[d].trend = (TextView) rootView.findViewById(getIdByName("trend" + day));
				mViewCache[d].var = (TextView) rootView.findViewById(getIdByName("var" + day));
				mViewCache[d].rung = (EditText) rootView.findViewById(getIdByName("rung" + day));
				mViewCache[d].flag = (CheckBox) rootView.findViewById(getIdByName("flag" + day));
				mViewCache[d].comment = (EditText) rootView.findViewById(getIdByName("comment" + day));
				mViewCache[d].row = (LinearLayout) rootView.findViewById(getIdByName("rowDay" + day));
				final int dayOfMonth = d;
				TextWatcher onChange = new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

					}

					@Override
					public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

					}

					@Override
					public void afterTextChanged(Editable editable) {
						if (!mCanSave) // do not save when updateEverything is running
							return;
						double weight;
						int rung;
						try {
							weight = Double.parseDouble(mViewCache[dayOfMonth].weight.getText().toString());
						} catch (Exception e) {
							weight = 0.0f;
						}
						try {
							rung = Integer.parseInt(mViewCache[dayOfMonth].rung.getText().toString());
						} catch (Exception e) {
							rung = 0;
						}
						mWeight.add(mItem.year, mItem.month, dayOfMonth, weight, rung, mViewCache[dayOfMonth].flag.isChecked(), mViewCache[dayOfMonth].comment.getText().toString());
						MonthListActivity.mChanged = true;
					}
				};
				CompoundButton.OnCheckedChangeListener onCheck = new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
						if (!mCanSave) // do not save when updateEverything is running
							return;
						double weight;
						int rung;
						try {
							weight = Double.parseDouble(mViewCache[dayOfMonth].weight.getText().toString());
						} catch (Exception e) {
							weight = 0.0f;
						}
						try {
							rung = Integer.parseInt(mViewCache[dayOfMonth].rung.getText().toString());
						} catch (Exception e) {
							rung = 0;
						}
						mWeight.add(mItem.year, mItem.month, dayOfMonth, weight, rung, mViewCache[dayOfMonth].flag.isChecked(), mViewCache[dayOfMonth].comment.getText().toString());
						MonthListActivity.mChanged = true;
					}
				};

				mViewCache[d].weight.setOnFocusChangeListener(onBlur);
				mViewCache[d].rung.setOnFocusChangeListener(onBlur);
				mViewCache[d].flag.setOnFocusChangeListener(onBlur);
				mViewCache[d].comment.setOnFocusChangeListener(onBlur);

				mViewCache[d].weight.addTextChangedListener(onChange);
				mViewCache[d].rung.addTextChangedListener(onChange);
				mViewCache[d].comment.addTextChangedListener(onChange);

				mViewCache[d].flag.setOnCheckedChangeListener(onCheck);

				if (d == mToday.get(Calendar.DAY_OF_MONTH) && mToday.get(Calendar.YEAR) == mItem.year && mToday.get(Calendar.MONTH) + 1 == mItem.month) {
					mViewCache[d].weight.requestFocus();
				}
			}
			viewCachePopulated = true;
		}

		//LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.weightGraph);
		//layout.addView(graphView);

		updateEverything();

		return rootView;
	}
}
