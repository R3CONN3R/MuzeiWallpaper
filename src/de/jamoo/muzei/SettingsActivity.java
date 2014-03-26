package de.jamoo.muzei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.jamoo.muzei.ui.hhmmpicker.HHmsPickerBuilder;
import de.jamoo.muzei.ui.hhmmpicker.HHmsPickerDialogFragment;

public class SettingsActivity extends FragmentActivity implements HHmsPickerDialogFragment.HHmsPickerDialogHandler {

	private static final String TAG = "SettingsActivity";

	private TextView mConfigConnection;
	private TextView mConfigFreq;

	private LinearLayout mSourceContainer;
	private HashSet<String> mSelectedSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		PreferenceHelper.limitConfigFreq(this);

		setContentView(R.layout.activity_settings);

		setupActionBar();

		mConfigConnection = (TextView) findViewById(R.id.config_connection);
		mConfigFreq = (TextView) findViewById(R.id.config_freq);

		mSourceContainer = (LinearLayout) findViewById(R.id.sourceContainer);

		setupList();
		setupConfig();

	}

	private void setupConfig() {
		mConfigFreq.setOnClickListener(mOnConfigFreqClickListener);
		mConfigConnection.setOnClickListener(mOnConfigConnectionClickListener);

		updateConfigFreq();
		updateConfigConnection();
		updateConfigSource();
	}

	private void updateConfigFreq() {
		Log.d(TAG, "updateConfigFreq");
		int configFreq = PreferenceHelper.getConfigFreq(this);
		mConfigFreq.setText(getString(R.string.config_every, Utils.convertDurationtoString(configFreq)));
		// Send an intent to communicate the update with the service
		Intent intent = new Intent(this, WallSource.class);
		intent.putExtra("configFreq", configFreq);
		startService(intent);
	}

	private void updateConfigConnection() {
		switch (PreferenceHelper.getConfigConnection(this)) {
		case PreferenceHelper.CONNECTION_ALL:
			mConfigConnection.setText(R.string.config_connection_all);
			mConfigConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_config_connection_all, 0, 0, 0);
			break;
		case PreferenceHelper.CONNECTION_WIFI:
			mConfigConnection.setText(R.string.config_connection_wifi);
			mConfigConnection.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_config_connection_wifi, 0, 0, 0);
			break;
		}
	}

	private void updateConfigSource() {

		int n = mSourceContainer.getChildCount();
		if(mSourceContainer.getChildAt(0).getId() == R.id.empty) {
			return;
		}

		List<String> list = PreferenceHelper.selectedCategoriesFromPref(this);
		mSelectedSet = new HashSet<String>();
		for (String s : list) {
			//Log.d(TAG, "add " + s);
			mSelectedSet.add(s);
		}

		for (int i=0;i<n;i++) {
			CheckedTextView tv = (CheckedTextView) mSourceContainer.getChildAt(i);
			tv.setChecked(mSelectedSet.contains((String)tv.getText()));
		}
	}

	private View.OnClickListener mOnConfigFreqClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "onConfigFreqClickListener");
			HHmsPickerBuilder hpb = new HHmsPickerBuilder()
			.setFragmentManager(getSupportFragmentManager())
			.setStyleResId(R.style.BetterPickersDialogFragment);
			hpb.show();
		}
	};

	@Override
	public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
		Log.d(TAG, "onDialogHmsSet");
		int duration = hours * 3600000 + minutes * 60000 + seconds * 1000;
		if(duration >= PreferenceHelper.MIN_FREQ_MILLIS) {
			PreferenceHelper.setConfigFreq(this, duration);
			updateConfigFreq();
		} else {
			Toast.makeText(this, "Minimum refresh rate is 5min ", Toast.LENGTH_LONG).show();
		}
	}

	private View.OnClickListener mOnConfigConnectionClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int connection = PreferenceHelper.getConfigConnection(SettingsActivity.this);
			if(connection == PreferenceHelper.CONNECTION_WIFI) {
				mConfigConnection.setText(R.string.config_connection_all);
				PreferenceHelper.setConfigConnection(SettingsActivity.this, PreferenceHelper.CONNECTION_ALL);
			} else if(connection == PreferenceHelper.CONNECTION_ALL) {
				mConfigConnection.setText(R.string.config_connection_wifi);
				PreferenceHelper.setConfigConnection(SettingsActivity.this, PreferenceHelper.CONNECTION_WIFI);
			}
			updateConfigConnection();
		}
	};

	private void setupList() {

		List<String> available = PreferenceHelper.categoriesFromPref(this);

		Log.d(TAG, "available.size: " +available.size());

		if(available.size() == 0){
			return;
		}
		mSourceContainer.removeAllViews();
		LayoutInflater inflater = getLayoutInflater();
		for (String item : available) {
			mSourceContainer.addView(newSourceRow(inflater, item));
		}
	}

	private View newSourceRow(LayoutInflater inflater, String item) {
		CheckedTextView v = (CheckedTextView) inflater.inflate(R.layout.category, null);
		v.setText(item);
		v.setOnClickListener(mSourceRowClickListener);
		return v;
	}

	View.OnClickListener mSourceRowClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			CheckedTextView tv = (CheckedTextView) v;
			tv.toggle();
			PreferenceHelper.selectedCategoriesToPref(SettingsActivity.this, getUiSelectedSource());
		}
	};

	private List<String> getUiSelectedSource() {
		ArrayList<String> results = new ArrayList<String>();
		int n = mSourceContainer.getChildCount();
		for (int i=0;i<n;i++) {
			CheckedTextView tv = (CheckedTextView)mSourceContainer.getChildAt(i);
			if (tv.isChecked()) {
				results.add((String) tv.getText());
			}
		}
		return results;
	}

	private void setupActionBar() {
		final LayoutInflater inflater = getLayoutInflater();
		View actionBarView = inflater.inflate(R.layout.ab_activity_settings, null);
		actionBarView.findViewById(R.id.actionbar_done).setOnClickListener(mOnActionBarDoneClickListener);
		getActionBar().setCustomView(actionBarView);
	}

	private View.OnClickListener mOnActionBarDoneClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
