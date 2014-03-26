package de.jamoo.muzei;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PreferenceHelper {

	public static final int CONNECTION_WIFI = 0;
	public static final int CONNECTION_ALL = 1;

	public static final int MIN_FREQ_MILLIS = 5 * 60 * 1000;

	private static final int DEFAULT_FREQ_MILLIS = 24 * 60 * 60 * 1000;

	private static String PREFERENCE_NAME ="my_walls";//You can change this but you don't have to

	private static String CONFIG_CONNECTION ="config_connection";
	private static String CONFIG_FREQ ="config_freq";
	private static String CATEGORIES ="categories";
	private static String SELECTED_CATEGORIES ="selected_categories";

	public static int getConfigConnection(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getInt(CONFIG_CONNECTION, CONNECTION_ALL);
	}

	public static void setConfigConnection(Context context, int connection) {
		SharedPreferences preferences = getPreferences(context);
		preferences.edit().putInt(CONFIG_CONNECTION, connection).commit();
	}

	public static int getConfigFreq(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getInt(CONFIG_FREQ, DEFAULT_FREQ_MILLIS);
	}

	public static void setConfigFreq(Context context, int durationMillis) {
		SharedPreferences preferences = getPreferences(context);
		preferences.edit().putInt(CONFIG_FREQ, durationMillis).commit();
	}

	public static void limitConfigFreq(Context context) {
		int configFreq = getConfigFreq(context);
		if(configFreq < MIN_FREQ_MILLIS) {
			setConfigFreq(context, MIN_FREQ_MILLIS);
		}
	}

	public static List<String> categoriesFromPref(Context context) {
		ArrayList<String> categories = new ArrayList<String>();
		SharedPreferences preferences = getPreferences(context);
		String prefCategories = preferences.getString(CATEGORIES, "");
		if(!TextUtils.isEmpty(prefCategories)) {
			try {
				JSONArray jsonArray = new JSONArray(prefCategories);
				for(int index = 0 ; index < jsonArray.length() ; index++) {
					categories.add(jsonArray.getString(index));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return categories;
	}

	public static List<String> selectedCategoriesFromPref(Context context) {
		ArrayList<String> categories = new ArrayList<String>();
		SharedPreferences preferences = getPreferences(context);
		String prefCategories = preferences.getString(SELECTED_CATEGORIES, "");
		if(!TextUtils.isEmpty(prefCategories)) {
			try {
				JSONArray jsonArray = new JSONArray(prefCategories);
				for(int index = 0 ; index < jsonArray.length() ; index++) {
					categories.add(jsonArray.getString(index));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return categories;
	}

	public static void categoriesToPref(Context context, List<String> categories) {
		SharedPreferences preferences = getPreferences(context);
		JSONArray jsonArray = new JSONArray(categories);
		preferences.edit().putString(CATEGORIES, jsonArray.toString()).commit();
	}

	public static void selectedCategoriesToPref(Context context, List<String> categories) {
		SharedPreferences preferences = getPreferences(context);
		JSONArray jsonArray = new JSONArray(categories);
		preferences.edit().putString(SELECTED_CATEGORIES, jsonArray.toString()).commit();
	}

	private static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
	}
}
