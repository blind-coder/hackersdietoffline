package de.anderdonau.hackersdiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class ExcerciseListActivity extends FragmentActivity implements ExcerciseListFragment.Callbacks {
    private boolean mTwoPane = false; // running on tablet?
    private AdView  adView   = null;

    public static Context mContext    = null;

    public static Context getAppContext() { return ExcerciseListActivity.mContext; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExcerciseListActivity.mContext = getApplicationContext();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_excercise_list);

        /**
         * Check for possibility of displaying ads
         */
        if (adView == null){
            adView = (AdView) findViewById(R.id.adViewExcercise);
        }
        if (adView != null){
            /* additional check for cheatcode */
            int check = isGooglePlayServicesAvailable(this);
            if (check != 0){
                GooglePlayServicesUtil.getErrorDialog(check, this, 0);
            } else {
                LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayoutExcercise);
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

        /**
         * Is this twoPane mode?
         */
        if (findViewById(R.id.excercise_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ExcerciseListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.excercise_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ExcerciseDetailFragment.ARG_ITEM_ID, id);
            ExcerciseDetailFragment fragment = new ExcerciseDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.excercise_detail_container, fragment).commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ExcerciseDetailActivity.class);
            detailIntent.putExtra(ExcerciseDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onBackPressed(){
        finish();
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
    }

    @Override
    public void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }

}
