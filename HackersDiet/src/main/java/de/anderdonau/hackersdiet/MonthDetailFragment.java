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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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
public class MonthDetailFragment extends Fragment
	implements View.OnClickListener {
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
		Class<R.id> res = R.id.class;
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

	public void onClick(View view)
	{
		int id = -1;
		for (int i = 1; i<=31; i++) {
			if (view.getId() == getIdByName("row" + (i < 10 ? "0" : "") + i)) {
				id = i;
				break;
			}
		}
		if (id == -1){
			return;
		}
		Calendar editDay = (Calendar)mToday.clone();
		editDay.set(Calendar.DAY_OF_MONTH, id);

		DialogNewData dialog = new DialogNewData(getActivity(), this.mWeight,
				this, editDay.get(Calendar.DAY_OF_MONTH),
				editDay.get(Calendar.MONTH), editDay.get(Calendar.YEAR));
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	/**
	 * Main drawing-handling function. Updates all widgets and text
	 */
	@SuppressLint("DefaultLocale")
	public void updateEverything() {
		mCanSave = false; // while this is running, we prevent saving. Works as a mutex.
		int year = mItem.year;
		int month = mItem.month;
		int wholeDate = year * 10000 + month * 100 + 1;
		int wholeDateLast = year * 10000 + month * 100 + mWeight.daysInMonth(month, year);

		for (int d = 1; d <= 31; d++) {
			/*
			 * Hide all widgets that are not used in this month and set them to empty strings or false.
			 */
			mViewCache[d].row.setVisibility(View.GONE);
			if (d <= mWeight.daysInMonth(month, year)) {
				/*
				 * Obviously only necessary for days 29 and 30 for feb, and 31 for apr, jun, sep, nov.
				 */
				mViewCache[d].row.setVisibility(View.VISIBLE);
			}
			mViewCache[d].weight.setText("");
			mViewCache[d].trend.setText("");
			mViewCache[d].var.setText("");
			mViewCache[d].comment.setText("");
			mViewCache[d].row.setOnClickListener(this);
		}
		mToday.set(Calendar.DAY_OF_MONTH, mWeight.daysInMonth(month - 1, year));

		Calendar tmpDate = new GregorianCalendar(year, month - 1, 1);
		double max = -1;
		double min = 99999;

		SimpleDateFormat sdfDay = new SimpleDateFormat("MMMM yyyy");
		((TextView) rootView.findViewById(R.id.textMonth)).setText(sdfDay.format(tmpDate.getTime()));

		mPtr = mWeight.allData;

		if (mPtr.wholedate < wholeDate) { // mPtr is before today
			while (mPtr.wholedate < wholeDate && mPtr.next != null) {
				mPtr = mPtr.next;
			}
			if (mPtr.wholedate < wholeDate) { // No entries for this month
				mCanSave = true;
				return;
			}
		} else if (mPtr.wholedate > wholeDate) { // mPtr is beyond today
			while (mPtr.wholedate > wholeDate && mPtr.prev != null) {
				mPtr = mPtr.prev;
			}
			if (mPtr.wholedate > wholeDateLast) { // No entries for this month
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
		for (; mPtr != null && mPtr.wholedate <= wholeDateLast; mPtr = mPtr.next) { // fill all values
			numWeight = mPtr.wholedate % (year * 10000 + month * 100);
			max = Math.max(max, mPtr.getTrend());
			min = Math.min(min, mPtr.getTrend());
			if (mPtr.getWeight() > 0.0f) { // only care if we have an actual value
				max = Math.max(max, mPtr.getWeight());
				min = Math.min(min, mPtr.getWeight());
				weightValues.appendData(new DataPoint(numWeight, mPtr.getWeight()), false, 31);
				trendValues.appendData(new DataPoint(numWeight, mPtr.getTrend()), false, 31);
			} else {
				weightValues.appendData(new DataPoint(numWeight, Double.NaN), false, 31);
				trendValues.appendData(new DataPoint(numWeight, Double.NaN), false, 31);
			}

			if (mPtr.rung > 0) {
				rungValues.appendData(new DataPoint(numWeight, mPtr.rung), false, 31);
				numRung++;
			}

			if (mPtr.getVar() > 0) {
				mViewCache[numWeight].var.setTextColor(getResources().getColor(R.color.weightGain, null));
			} else if (mPtr.getVar() < 0) {
				mViewCache[numWeight].var.setTextColor(getResources().getColor(R.color.weightLoss, null));
			} else {
				mViewCache[numWeight].var.setTextColor(getResources().getColor(R.color.weightConstant, null));
			}

			mViewCache[numWeight].weight.setHint("");
			if (mPtr.getWeight() > 0) {
				mViewCache[numWeight].weight.setText(String.format(getResources().getString(R.string.txtWeight), mPtr.getWeight()));
			}
			mViewCache[numWeight].trend.setText(String.format(getResources().getString(R.string.txtTrend), mPtr.getTrend()));
			mViewCache[numWeight].var.setText(String.format(getResources().getString(R.string.txtVar), mPtr.getVar()));
			mViewCache[numWeight].comment.setText(mPtr.comment.isEmpty() ? "" : "...");
			if (mPtr.flag){
				mViewCache[numWeight].row.setBackgroundColor(getResources().getColor(R.color.flagged));
			}
			tmpDate.set(Calendar.DAY_OF_MONTH, numWeight);
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
		/*
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

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = MonthListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        } else {
			return;
		}

		mToday = new GregorianCalendar();
		mWeight = MonthListActivity.getmWeightData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_month_detail_buttons, container, false);

		mPtr = mWeight.allData;

		if (!viewCachePopulated) {
			for (int d = 1; d <= 31; d++) {
				String day = "";
				if (d < 10) {
					day = "0";
				}
				day += String.valueOf(d);
				mViewCache[d] = new viewCache();
				mViewCache[d].weight = rootView.findViewById(getIdByName("txtWeight" + day));
				mViewCache[d].trend = rootView.findViewById(getIdByName("txtTrend" + day));
				mViewCache[d].var = rootView.findViewById(getIdByName("txtVar" + day));
				mViewCache[d].comment = rootView.findViewById(getIdByName("txtComment" + day));
				mViewCache[d].row = rootView.findViewById(getIdByName("row" + day));
			}
			viewCachePopulated = true;
		}

		updateEverything();
		return rootView;
	}
}
