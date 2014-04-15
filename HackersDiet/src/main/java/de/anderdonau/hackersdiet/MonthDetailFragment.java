package de.anderdonau.hackersdiet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
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

	/**
	 * Search a widget by its name and return its ID.
	 */
	public int getIdByName(String name){
		Class res = R.id.class;
		int id;
		try {
			Field field = res.getField(name);
			id = field.getInt(null);
		} catch (Exception e){ Log.d("getIdByName", "Cant get " + name); return -1; }
		return id;
	}

	/**
	 * Main drawing-handling function. Updates all widgets and text
	 */
	public void updateEverything(){
		mCanSave = false; // while this is running, we prevent saving. Works as a mutex.
		SimpleDateFormat sdfDay = new SimpleDateFormat("EEEE, d. MMMM yyyy");

		for (int d = 1; d <= 31; d++){
			if (d > 28 && d > mWeight.daysinmonth(mToday.get(Calendar.MONTH)+1, mToday.get(Calendar.YEAR))){
				/**
				 * Hide all widgets that are not used in this month and set them to empty strings or false.
				 */
				mViewCache[d].row.setVisibility(View.GONE);
				mViewCache[d].comment.setVisibility(View.GONE);
				mViewCache[d].textDay.setVisibility(View.GONE);
				mViewCache[d].weight.setText("");
				mViewCache[d].trend.setText(" ");
				mViewCache[d].var.setText(" ");
				mViewCache[d].rung.setText("");
				mViewCache[d].flag.setChecked(false);
				mViewCache[d].comment.setText("");
			} else if (d > 28){
				/**
				 * Obviously only necessary for days 29 and 30 for feb, and 31 for apr, jun, sep, nov.
				 */
				mViewCache[d].row.setVisibility(View.VISIBLE);
				mViewCache[d].comment.setVisibility(View.VISIBLE);
				mViewCache[d].textDay.setVisibility(View.VISIBLE);
			}
			if (d <= mWeight.daysinmonth(mToday.get(Calendar.MONTH)+1, mToday.get(Calendar.YEAR))){
				mToday.set(Calendar.DAY_OF_MONTH, d);
				mViewCache[d].textDay.setText(sdfDay.format(mToday.getTime()));
			}
		}

		int year          = mItem.year;
		int month         = mItem.month;
		int wholedate     = year*10000 + month*100 + 1;
		int wholedatelast = year*10000 + month*100 + mWeight.daysinmonth(month, year);
        Calendar tmpDate  = new GregorianCalendar(year-1900, month-1, 1);

		mPtr = mWeight.allData;

		if (mPtr.wholedate < wholedate){ // mPtr is before today
			while (mPtr.wholedate < wholedate && mPtr.next != null){
				mPtr = mPtr.next;
			}
			if (mPtr.wholedate < wholedate){ // No entries for this month
				mCanSave = true;
				return;
			}
		} else if (mPtr.wholedate > wholedate){ // mPtr is beyond today
			while (mPtr.wholedate > wholedate && mPtr.prev != null){
				mPtr = mPtr.prev;
			}
			if (mPtr.wholedate > wholedatelast){ // No entries for this month
				mCanSave = true;
				return;
			}
		}

		for (;mPtr != null && mPtr.wholedate <= wholedatelast; mPtr = mPtr.next){ // fill all values
			int d = mPtr.wholedate % (year*10000+month*100);

			mViewCache[d].weight.setText(String.valueOf(mPtr.weight));
			mViewCache[d].trend.setText(String.format("%.1f", mPtr.trend));

			if (mPtr.var > 0){
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightGain));
			} else if (mPtr.var < 0){
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightLoss));
			} else {
				mViewCache[d].var.setTextColor(getResources().getColor(R.color.weightConstant));
			}

			mViewCache[d].var.setText(String.format("%+.1f", mPtr.var));
			mViewCache[d].rung.setText(String.valueOf(mPtr.rung));
			mViewCache[d].flag.setChecked(mPtr.flag);
			mViewCache[d].comment.setText(mPtr.comment);

            tmpDate.set(Calendar.DAY_OF_MONTH, d);
            mViewCache[d].textDay.setText(sdfDay.format(tmpDate.getTime()));

        }
		mCanSave = true;
	}

	public MonthDetailFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = MonthListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_month_detail, container, false);

		mToday = new GregorianCalendar();
		mWeight = MonthListActivity.getmWeightData();
		mPtr = mWeight.allData;

		View.OnFocusChangeListener onBlur = new View.OnFocusChangeListener() {
			public void onFocusChange(View arg0, boolean hasFocus) {
				if (hasFocus){ // only update on loss of focus
					return;
				}
				if (MonthListActivity.mChanged){
					updateEverything();
				}
			}
		};

		if (!viewCachePopulated){
			for (int d = 1; d <= 31; d++){
				String day = "";
				if (d < 10){
					day = "0";
				}
				day += String.valueOf(d);
				mViewCache[d] = new viewCache();
				mViewCache[d].weight	= (EditText) rootView.findViewById(getIdByName("weight" + day));
				mViewCache[d].trend		= (TextView) rootView.findViewById(getIdByName("trend" + day));
				mViewCache[d].var		= (TextView) rootView.findViewById(getIdByName("var" + day));
				mViewCache[d].rung		= (EditText) rootView.findViewById(getIdByName("rung" + day));
				mViewCache[d].flag		= (CheckBox) rootView.findViewById(getIdByName("flag" + day));
				mViewCache[d].comment	= (EditText) rootView.findViewById(getIdByName("comment" + day));
				mViewCache[d].row		= (LinearLayout) rootView.findViewById(getIdByName("rowDay" + day));
				mViewCache[d].textDay	= (TextView) rootView.findViewById(getIdByName("textDay" + day));
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
						if (!mCanSave){ // do not save when updateEverything is running
                            Log.d("afterTextChanged", "adding because mCanSave is false");
							return;
						}
						mWeight.add(mToday.get(Calendar.YEAR), mToday.get(Calendar.MONTH)+1, dayOfMonth,
								mViewCache[dayOfMonth].weight.getText().toString(), mViewCache[dayOfMonth].rung.getText().toString(),
								mViewCache[dayOfMonth].flag.isChecked(), mViewCache[dayOfMonth].comment.getText().toString());
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

				if (d == mToday.get(Calendar.DAY_OF_MONTH) && mToday.get(Calendar.YEAR) == mItem.year && mToday.get(Calendar.MONTH)+1 == mItem.month){
					mViewCache[d].weight.requestFocus();
                }
			}
			viewCachePopulated = true;
		}
		updateEverything();

		return rootView;
	}
}
