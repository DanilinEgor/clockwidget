package com.example.clockwidget;

import java.io.IOException;

import com.danegor.clockwidget.R;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by egor on 24.04.14.
 */
public class ClockActions extends AppWidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
			/**
			 * Remove this code if you do not want to set wallpaper when widget is added
			 */
			WallpaperManager wm = WallpaperManager.getInstance(context);
			try {
				wm.setResource(R.drawable.background_wallpaper);
			} catch (IOException e) {
				Log.e("ClockWidget", "error", e);
			}
			/**
			 * 
			 */
		}
	}
}
