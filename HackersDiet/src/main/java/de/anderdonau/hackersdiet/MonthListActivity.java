package de.anderdonau.hackersdiet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;


/**
 * An activity representing a list of Months. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MonthDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MonthListFragment} and the item details
 * (if present) is a {@link MonthDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link MonthListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MonthListActivity extends FragmentActivity implements MonthListFragment.Callbacks {
	private boolean mTwoPane = false; // running on tablet?
    private AdView  adView   = null;

    public static Context    mContext    = null;
	public static weightData mWeightData = null;
	public static boolean    mChanged    = false; // will be true if any field has changed

    public MonthListFragment mFragment;

	public static Context getAppContext() { return MonthListActivity.mContext; }
	public static weightData getmWeightData(){ return MonthListActivity.mWeightData; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MonthListActivity.mContext = getApplicationContext();

        if (MonthListActivity.mWeightData == null){
            /**
             * Only load the data once on startup.
             */
            MonthListActivity.mWeightData = new weightData();
            MonthListActivity.mWeightData.loadData();
        }

        setContentView(R.layout.activity_month_list);

        /**
         * Check for possibility of displaying ads
         */
        if (adView == null){
            adView = (AdView) findViewById(R.id.adView);
        }
        if (adView != null){
            /* additional check for cheatcode */
            int check = isGooglePlayServicesAvailable(this);
            if (check != 0){
                GooglePlayServicesUtil.getErrorDialog(check, this, 0);
            } else {
                LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
                SharedPreferences settings = getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);
                final boolean hideAds = settings.getBoolean("hideads", false);

                if (hideAds){
                    adView.setVisibility(View.GONE);
                } else {
                    if (layout != null){
                        // Initiate a generic request.
                        AdRequest adRequest = new AdRequest.Builder()
                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
                                .build();

                        // Load the adView with the ad request.
                        adView.loadAd(adRequest);
                    }
                }
            }
        }
        mFragment = ((MonthListFragment) getSupportFragmentManager().findFragmentById(R.id.month_list));

        /**
         * Is this twoPane mode?
         */
        if (findViewById(R.id.month_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            mFragment.setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MonthDetailFragment.ARG_ITEM_ID, id);
            MonthDetailFragment fragment = new MonthDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.month_detail_container, fragment).commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MonthDetailActivity.class);
            detailIntent.putExtra(MonthDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				Intent settingsIntent = new Intent(this, Prefs.class);
				startActivity(settingsIntent);
				return true;
			case R.id.save:
				mWeightData.saveData();
				mChanged=false;
				return true;
			case R.id.menuAbout:
				AlertDialog.Builder about = new AlertDialog.Builder(this);
				about.setMessage(R.string.aboutHackDietOffline)
					.setCancelable(false)
					.setNeutralButton(R.string.thanks, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
				about.create().show();
				return true;
            case R.id.menuExcercise:
                Intent excerciseIntent = new Intent(this, ExcerciseListActivity.class);
                startActivity(excerciseIntent);
                return true;
		}
		return false;
	}

	@Override
	public void onBackPressed(){
		if (!mChanged){
			finish();
			return;
		}

		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setMessage(R.string.saveFirst)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mWeightData.saveData();
					dialog.cancel();
					finish();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					finish();
				}
			})
			.setNeutralButton(R.string.dontQuit, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id){
					dialog.cancel();
				}
			});
		AlertDialog alert = confirm.create();
		alert.show();
	}

    @Override
    public void onPause() {
        if (adView != null){
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null){
            adView.resume();
        }
        mFragment.updateList();
    }

    @Override
    public void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }

}
