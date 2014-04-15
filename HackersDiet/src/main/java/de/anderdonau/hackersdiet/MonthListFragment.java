package de.anderdonau.hackersdiet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A list fragment representing a list of Months. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MonthDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MonthListFragment extends ListFragment {
    int id = 0;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sMonthCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sMonthCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MonthListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        weightData mWeightData;
        weightDataDay mPtr;
        mWeightData = MonthListActivity.getmWeightData();
        Calendar mToday = new GregorianCalendar();

		if (MonthListContent.ITEMS.isEmpty()){
			int lastyear = 1970;
			int lastmonth = 1;
            mPtr = mWeightData.allData;
            while (mPtr.next != null){
                mPtr = mPtr.next;
            }
			for (; mPtr != null; mPtr = mPtr.prev){
				// mPtr.wholeDate = year*10000 + month * 100 + d;
				if (mPtr.year != lastyear || mPtr.month != lastmonth){
					id++;
					//Log.d("MLFragment.onCreate", String.format("%d", mPtr.wholedate));
					MonthListContent.addItem (new MonthListContent.MonthItem(String.format("%d", id), String.format("%4d/%02d", mPtr.year, mPtr.month), mPtr.year, mPtr.month));
					lastyear = mPtr.year;
					lastmonth = mPtr.month;
				}
			}
		}
        mPtr = mWeightData.allData;
        while (mPtr.next != null){
            mPtr = mPtr.next;
        }
        if ((mToday.get(Calendar.YEAR)+1900) * 10000 + (mToday.get(Calendar.MONTH)+1)*100 + mToday.get(Calendar.DAY_OF_MONTH) > mPtr.wholedate){
            id++;
            MonthListContent.addItem (new MonthListContent.MonthItem(String.format("%d", id),
                    String.format("%4d/%02d", mToday.get(Calendar.YEAR)+1900, mToday.get(Calendar.MONTH)+1),
                    mToday.get(Calendar.YEAR)+1900, mToday.get(Calendar.MONTH)+1));
        }

		setListAdapter(new ArrayAdapter<MonthListContent.MonthItem>(
					getActivity(),
					android.R.layout.simple_list_item_activated_1,
					android.R.id.text1,
					MonthListContent.ITEMS));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mCallbacks = sMonthCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(MonthListContent.ITEMS.get(position).id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick
				? ListView.CHOICE_MODE_SINGLE
				: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
