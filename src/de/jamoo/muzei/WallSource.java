/*
 * Copyright (C) 2014 Alex Besler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jamoo.muzei;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

public class WallSource extends RemoteMuzeiArtSource {

	private static final String SOURCE_NAME = "My Icon Pack Wallpapers";//TODO Change to your preferred name
	private static final boolean sortCategories = true;//TODO When true categories get sorted alphabetically

	public static final int COMMAND_ID_SHARE = 1337;

	private static final String TAG = "WallSource";
	private static final boolean DEBUG = true;

	public WallSource()
	{
		super(SOURCE_NAME);
	}

	public void onCreate()
	{
		if(DEBUG)Log.w(TAG, "onCreate");
		super.onCreate();

		ArrayList<UserCommand> commands = new ArrayList<UserCommand>();
		commands.add(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK));
		commands.add(new UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share_app)));

		setUserCommands(commands);

		PreferenceHelper.limitConfigFreq(this);
	}

	@Override
	public void onCustomCommand(int id) {
		super.onCustomCommand(id);

		if (id == COMMAND_ID_SHARE) {
			Artwork currentArtwork = getCurrentArtwork();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");

			Uri artUrl = currentArtwork.getImageUri();
			if(DEBUG)Log.d(TAG,"artUrl: " + artUrl);

			String author = currentArtwork.getByline();
			if(DEBUG)Log.d(TAG,"author: " + author);

			String playUrl = "http://play.google.com/store/apps/details?id=" + getPackageName();
			if(DEBUG)Log.d(TAG,"playUrl: " + playUrl);

			shareIntent.putExtra(Intent.EXTRA_TEXT,
					"My wallpaper today is " + currentArtwork.getTitle() + " by " + author + " \n"
							+ artUrl + " \n"
							+ "from the " + getString(R.string.app_name) + " app\n"
							+ "Get it now on the PlayStore! " + playUrl
					);

			shareIntent = Intent.createChooser(shareIntent, "Share Wallpaper");
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(shareIntent);
		}
	}

	protected void onTryUpdate(int reason) throws RetryException {
		if(DEBUG)Log.w(TAG, "onTryUpdate");

		if (!isConnectedAsPreferred()) {
			scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
			if(DEBUG)Log.v(TAG, "Cancelled! Wrong connection!");
			return;
		}

		String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

		String WALL_URL = getString(R.string.config_wallpaper_manifest_url);

		JSON response = getJson(WALL_URL);

		if (response == null) {
			throw new RetryException();
		}

		if (response.All.size() == 0) {
			Log.w(TAG, "Whoops! No walls returned!");
			scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
			return;
		}

		List<String> categories = getCategories(response);

		PreferenceHelper.categoriesToPref(WallSource.this, categories);

		ArrayList<NodeWallpaper> wallpapers = getValidWalls(response);

		if(wallpapers.size() == 0 || wallpapers == null){
			Log.i(TAG, "No valid walls returned.");
			scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
			return;			
		}

		Random random = new Random();
		NodeWallpaper wall;
		String token;

		while (true) {
			wall = wallpapers.get(random.nextInt(wallpapers.size()));
			token = wall.url;
			if (wallpapers.size() <= 1 || !TextUtils.equals(token, currentToken)) {
				Log.i(TAG, "Selected wall: " + wall.name);
				break;
			}
		};

		publishArtwork(new Artwork.Builder()
		.title(wall.name)
		.byline(wall.author)
		.imageUri(Uri.parse(wall.url))
		.token(token)
		.viewIntent(new Intent(Intent.ACTION_VIEW,
				Uri.parse(wall.url)))
				.build());

		scheduleUpdate(System.currentTimeMillis() + getRotateTimeMillis());
	}

	private int getRotateTimeMillis() {
		return PreferenceHelper.getConfigFreq(this);
	}

	private boolean isConnectedAsPreferred() {
		if(PreferenceHelper.getConfigConnection(this) == PreferenceHelper.CONNECTION_WIFI) {
			return Utils.isWifiConnected(this);
		}
		return true;
	}

	private JSON getJson(String WALL_URL) {

		JSON response = null;
		InputStream in = null;

		try {
			final URL url = new URL(WALL_URL);
			in = new BufferedInputStream(url.openStream());
			response = new JSON(Utils.toString(in));
		} catch (final IOException ignore) {} catch (final JSONException ignore) {}
		finally
		{
			Utils.close(in);
		}

		return response;
	}

	private List<String> getCategories(JSON response){

		List<String> categories = new ArrayList<String>();

		for (int i = 0; i < response.All.size(); i++) {
			final NodeCategory node = response.All.get(i);

			boolean isNull = (node.name == null || node.name.isEmpty()) ? true : false;

			if(!isNull)
			{
				if(!node.name.equals("All"))categories.add(node.name);
			}
		}

		if(sortCategories)Collections.sort(categories);

		return categories;
	}

	private ArrayList<NodeWallpaper> getValidWalls(JSON response){
		ArrayList<NodeWallpaper> wallpapers = new ArrayList<NodeWallpaper>();
		List<String> selectedCategories = PreferenceHelper.selectedCategoriesFromPref(WallSource.this);

		for(NodeCategory node : response.All){

			boolean isNull = (node.name == null || node.name.isEmpty()) ? true : false;

			if(!isNull)
			{
				if(node.name.equals("All") && selectedCategories.size() == 0){
					Log.v(TAG, "No selected categories. Adding all");
					wallpapers.addAll(node.wallpaperList);
					break;

				} else if (selectedCategories.contains(node.name)) {
					Log.i(TAG, "Valid node: " + node.name + " size: " +node.wallpaperList.size());

					wallpapers.addAll(node.wallpaperList);

				} else {
					Log.w(TAG, "Invalid node: " + node.name + " size: " +node.wallpaperList.size());
				}
			}
		}

		return wallpapers;

	}
}