package de.anderdonau.hackersdiet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * An activity representing a single Excercise detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ExcerciseListActivity}.
 *
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ExcerciseDetailFragment}.
 */
public class ExcerciseDetailActivity extends FragmentActivity {
    ExcerciseDetailFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ExcerciseDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(ExcerciseDetailFragment.ARG_ITEM_ID));
            fragment = new ExcerciseDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.excercise_detail_container, fragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ExcerciseListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnAddRungToday(View view){
        weightData w = MonthListActivity.getmWeightData();
        Calendar today = new GregorianCalendar();
        weightDataDay wd = w.getByDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1, today.get(Calendar.DAY_OF_MONTH));
        wd.rung = Integer.parseInt(getIntent().getStringExtra(ExcerciseDetailFragment.ARG_ITEM_ID));
        w.add(wd);
        MonthListActivity.mChanged = true;
        Toast.makeText(this, String.format(getString(R.string.toastAddedRungToTodaysEntry), wd.rung), Toast.LENGTH_SHORT).show();
    }
}
