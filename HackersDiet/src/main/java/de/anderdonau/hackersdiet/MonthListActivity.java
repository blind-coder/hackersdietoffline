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

/**
 * An activity representing a list of Months. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MonthDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MonthListFragment} and the item details
 * (if present) is a {@link MonthDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MonthListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MonthListActivity extends FragmentActivity implements MonthListFragment.Callbacks {
	public static Context           mContext    = null;
	public static weightData        mWeightData = null;
	public static boolean           mChanged    = false; // will be true if any field has changed
	public        MonthListFragment mFragment   = null;
	private       boolean           mTwoPane    = false; // running on tablet?

	public static Context getAppContext() {
		return MonthListActivity.mContext;
	}

	public static weightData getmWeightData() {
		return MonthListActivity.mWeightData;
	}

	public static void checkSaveData() {
		if (mChanged) {
			SharedPreferences settings = mContext.getSharedPreferences("de.anderdonau.hackdiet.prefs", 0);
			boolean autoSave = settings.getBoolean("autosave", true);

			if (autoSave) {
				mWeightData.saveData();
				mChanged = false;
			}
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
			getSupportFragmentManager().beginTransaction().replace(R.id.month_detail_container, fragment)
				.commit();
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
		if (item.getItemId() == R.id.settings) {
			Intent settingsIntent = new Intent(this, Prefs.class);
			startActivity(settingsIntent);
			return true;
		}
		if (item.getItemId() == R.id.save) {
			mWeightData.saveData();
			mChanged = false;
			return true;
		}
		if (item.getItemId() == R.id.menuAbout) {
			AlertDialog.Builder about = new AlertDialog.Builder(this);
			StringBuilder msg = new StringBuilder();
			msg.append(getString(R.string.aboutHackDietOffline));
			msg.append("\n");
			try {
				long versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode();
				String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				msg.append("VersionCode: ").append(versionCode).append("\n").
						append("VersionName: ").append(versionName);
			} catch (Exception ignored){
				msg.append("VersionCode: unknown\nVersionName: unknown");
			}
			about.setMessage(msg.toString()).setCancelable(false).setNeutralButton(
					R.string.thanks, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}
			);
			about.create().show();
			return true;
		}
		if (item.getItemId() == R.id.menuExcercise) {
			Intent exerciseIntent = new Intent(this, ExcerciseListActivity.class);
			startActivity(exerciseIntent);
			return true;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		checkSaveData();

		if (!mChanged) {
			finish();
			return;
		}

		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setMessage(R.string.saveFirst).setCancelable(false).setPositiveButton(R.string.yes,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mWeightData.saveData();
					dialog.cancel();
					finish();
				}
			}
		).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				finish();
			}
		}).setNeutralButton(R.string.dontQuit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = confirm.create();
		alert.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MonthListActivity.mContext = getApplicationContext();

		if (MonthListActivity.mWeightData == null) {
			/**
			 * Only load the data once on startup.
			 */
			MonthListActivity.mWeightData = new weightData();
			MonthListActivity.mWeightData.loadData();
		}

		setContentView(R.layout.activity_month_list);

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
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mFragment.updateList();
	}
}
