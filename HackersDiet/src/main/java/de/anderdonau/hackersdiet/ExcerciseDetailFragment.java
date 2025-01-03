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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Exercise detail screen.
 * This fragment is either contained in a {@link ExcerciseListActivity}
 * in two-pane mode (on tablets) or a {@link ExcerciseDetailActivity}
 * on handsets.
 */
public class ExcerciseDetailFragment extends Fragment {
	View rootView = null;
	public static final String ARG_ITEM_ID = "item_id";
	private ExcerciseListContent.ExcerciseItem mItem;

	public ExcerciseDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = ExcerciseListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
		/*
		 * TODO: What do we do if mItem is unset?
		 */
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_excercise_detail, container, false);

		int bends = (int) Math.round(1 + 0.897602 * mItem.rung + 0.0584416 * mItem.rung * mItem.rung);
		int situps = (int) Math.round(1.7723 + 1.2488 * mItem.rung + 0.01998 * mItem.rung * mItem.rung);
		int leglifts = (int) Math.round(3 + 0.897602 * mItem.rung + 0.0584416 * mItem.rung * mItem.rung);
		int pushups = (int) Math.round(1.47727 + 0.501748 * mItem.rung + 0.0402098 * mItem.rung * mItem.rung);
		int runjumps = (int) (Math.floor((76.7045 + 33.5427 * mItem.rung - 0.618132 * mItem.rung * mItem.rung) / 5) * 5);

		if (mItem.rung > 15) {
			bends = (int) Math.round(12.4318 + 1.22752 * (mItem.rung - 15) + 0.0247253 * (mItem.rung - 15) * (mItem.rung - 15));
			situps = (int) Math.round(9.5 + 0.691309 * (mItem.rung - 15) + 0.033966 * (mItem.rung - 15) * (mItem.rung - 15));
			leglifts = (int) Math.round(10.9773 + 1.4533 * (mItem.rung - 15) + 0.0337163 * (mItem.rung - 15) * (mItem.rung - 15));
			pushups = 8 + mItem.rung - 15;
			runjumps = (int) Math.floor((322.045 + 19.2108 * (mItem.rung - 15) - 0.404595 * (mItem.rung - 15) * (mItem.rung - 15)) / 5) * 5;
		}

		if (mItem.rung >= 43) {
			runjumps = 560;
		}

		if (mItem.rung >= 46) {
			runjumps = 575;
		}

		TextView e;
		e = rootView.findViewById(R.id.textBends);
		e.setText(String.format("%d", bends));

		e = rootView.findViewById(R.id.textSitups);
		e.setText(String.format("%d", situps));

		e = rootView.findViewById(R.id.textLegLifts);
		e.setText(String.format("%d", leglifts));

		e = rootView.findViewById(R.id.textPushups);
		e.setText(String.format("%d", pushups));

		e = rootView.findViewById(R.id.textRuns);
		e.setText(String.format("%d - %d/%d", runjumps, runjumps / 75, runjumps % 75));

		e = rootView.findViewById(R.id.textExcerciseRung);
		e.setText(String.format("%d", mItem.rung));
		return rootView;
	}
}
