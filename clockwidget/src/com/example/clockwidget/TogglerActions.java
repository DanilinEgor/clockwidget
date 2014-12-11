package com.example.clockwidget;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.danegor.clockwidget.R;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by egor on 25.04.14.
 */
public class TogglerActions extends AppWidgetProvider {
	/**
	 * Actions for broadcast receiver of this class
	 */
    private static String WIFI_CLICK = "wifi_click";
    private static String GPS_CLICK = "gps_click";
    private static String BLUETOOTH_CLICK = "bluetooth_click";
    private static String ROTATION_CLICK = "rotation_click";
    private static String THREE_G_CLICK = "three_g_click";
    private static String SOUND_CLICK = "sound_click";
    private static String BRIGHTNESS_CLICK = "brightness_click";
    private static String FLASHLIGHT_CLICK = "flaslight_click";
    private static String ROTATION_SWITCHED = "rotation_switched";
    private static String BRIGHTNESS_MODE_SWITCHED = "brightness_switched";
    private static String SETTINGS_CLICK = "settings_click";
    Context _context;
    int mAppWidgetId;
    boolean[] on;
    List<TOGGLER> togglers = new ArrayList<TOGGLER>();
    List<Integer> ringerModes = new ArrayList<Integer>(Arrays.asList(AudioManager.RINGER_MODE_NORMAL, AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE));
    List<Integer> ringerImages = new ArrayList<Integer>(Arrays.asList(R.drawable.icon_sound_normal, R.drawable.icon_sound_silent, R.drawable.icon_sound_vibrate));
    static int current_brightness_image;
    static int wifi_image, wifi_icon, gps_image, gps_icon, bluetooth_image, bluetooth_icon, rotate_image, rotate_icon, three_g_image, three_g_icon,
            brightness_image, brightness_icon, airplane_image, airplane_icon, sound_image, sound_icon, flashlight_image, flashlight_icon;
    static Camera cam = null;

    public enum TOGGLER {wifi, gps, bluetooth, rotate, three_g, brightness, sound, flashlight};

    private ContentObserver rotationContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Intent intent = new Intent(_context, TogglerActions.class);
            intent.setAction(ROTATION_SWITCHED);
            _context.sendBroadcast(intent);
        }
    };

    private ContentObserver brightnessContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Intent intent = new Intent(_context, TogglerActions.class);
            intent.setAction(BRIGHTNESS_MODE_SWITCHED);
            _context.sendBroadcast(intent);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        _context = context;
        final String action = intent.getAction();
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.togglers_layout);

        if ("TogglerActions.APPWIDGET_ENABLED".equals(action)) {
        	
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

            remoteViews.setInt(R.id.first_image, "setVisibility", View.VISIBLE);
            remoteViews.setInt(R.id.second_image, "setVisibility", View.VISIBLE);
            remoteViews.setInt(R.id.third_image, "setVisibility", View.VISIBLE);
            remoteViews.setInt(R.id.fourth_image, "setVisibility", View.VISIBLE);
            remoteViews.setInt(R.id.fifth_image, "setVisibility", View.VISIBLE);

            on = intent.getExtras().getBooleanArray("on");
            mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            /**
             * Check, what toggles are on
             */
            int n = 0;
            for (int i = 0; i < on.length; ++i)
                if (on[i]) {
                    ++n;
                    togglers.add(TOGGLER.values()[i]);
                }

            List<String> images = new ArrayList<String>(Arrays.asList(new String[]{"first", "second", "third", "fourth", "fifth"}));

            /**
             * Hide some images, if toggles count is less than 5
             */
            switch (n) {
                case 0:
                	remoteViews.setInt(R.id.third_image, "setVisibility", View.GONE);
                    images.remove("third");
                case 1:
                	remoteViews.setInt(R.id.second_image, "setVisibility", View.GONE);
                	images.remove("second");
                case 2:
                	remoteViews.setInt(R.id.fourth_image, "setVisibility", View.GONE);
                    images.remove("fourth");
                case 3:
                	remoteViews.setInt(R.id.first_image, "setVisibility", View.GONE);
                    images.remove("first");
                case 4:
                	remoteViews.setInt(R.id.fifth_image, "setVisibility", View.GONE);
                    images.remove("fifth");
                case 5:
                    break;
            }

            /**
             * Set onClick intents for each toggle
             */           
            for (int i = 0; i < togglers.size(); ++i) {
                TOGGLER toggler = togglers.get(i);
                switch (toggler) {
                    case wifi:
                        wifi_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        wifi_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent wifiIntent = new Intent(context, TogglerActions.class);
                        wifiIntent.setAction(WIFI_CLICK);

                        PendingIntent wifiPendingIntent = PendingIntent.getBroadcast(context, 0, wifiIntent, 0);

                        remoteViews.setOnClickPendingIntent(wifi_image, wifiPendingIntent);

                        //check wifi
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        if (wifiManager != null && wifiManager.isWifiEnabled())
                            remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_on);
                        else
                            remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_off);
                        break;
                    case gps:
                        gps_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        gps_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent gpsIntent = new Intent(context, TogglerActions.class);
                        gpsIntent.setAction(GPS_CLICK);

                        PendingIntent gpsPendingIntent = PendingIntent.getBroadcast(context, 0, gpsIntent, 0);

                        remoteViews.setOnClickPendingIntent(gps_image, gpsPendingIntent);

                        //check gps
                        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            remoteViews.setImageViewResource(gps_icon, R.drawable.icon_gps_on);
                        else
                            remoteViews.setImageViewResource(gps_icon, R.drawable.icon_gps_off);
                        break;
                    case bluetooth:
                        bluetooth_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        bluetooth_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());
                        Toast.makeText(context, images.get(i), Toast.LENGTH_SHORT).show();

                        Intent bluetoothIntent = new Intent(context, TogglerActions.class);
                        bluetoothIntent.setAction(BLUETOOTH_CLICK);

                        PendingIntent bluetoothPendingIntent = PendingIntent.getBroadcast(context, 0, bluetoothIntent, 0);

                        remoteViews.setOnClickPendingIntent(bluetooth_image, bluetoothPendingIntent);

                        //check bluetooth
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled())
                            remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_on);
                        else
                            remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_off);

                        break;
                    case rotate:
                        rotate_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        rotate_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent rotationIntent = new Intent(context, TogglerActions.class);
                        rotationIntent.setAction(ROTATION_CLICK);

                        PendingIntent rotationPendingIntent = PendingIntent.getBroadcast(context, 0, rotationIntent, 0);
                        remoteViews.setOnClickPendingIntent(rotate_image, rotationPendingIntent);

                        // check rotation
                        if (android.provider.Settings.System.getInt(context.getContentResolver(),
                                Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
                            remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_on);
                        else
                            remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_off);

                        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                                true, rotationContentObserver);
                        break;
                    case three_g:
                        three_g_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        three_g_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent threegIntent = new Intent(context, TogglerActions.class);
                        threegIntent.setAction(THREE_G_CLICK);

                        PendingIntent threegPendingIntent = PendingIntent.getBroadcast(context, 0, threegIntent, 0);

                        remoteViews.setOnClickPendingIntent(three_g_image, threegPendingIntent);

                        //check 3g
                        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (manager != null && manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting())
                            remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_on);
                        else
                            remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_off);

                        break;
                    case brightness:
                        brightness_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        brightness_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent brightnessIntent = new Intent(context, TogglerActions.class);
                        brightnessIntent.setAction(BRIGHTNESS_CLICK);

                        PendingIntent brightnessPendingIntent = PendingIntent.getBroadcast(context, 0, brightnessIntent, 0);

                        remoteViews.setOnClickPendingIntent(brightness_image, brightnessPendingIntent);

                        try {
                            if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                                remoteViews.setImageViewResource(brightness_icon, R.drawable.icon_brightness_auto);
                                current_brightness_image = R.drawable.icon_brightness_auto;
                            } else {
                                remoteViews.setImageViewResource(brightness_icon, R.drawable.icon_brightness_85);
                                current_brightness_image = R.drawable.icon_brightness_85;
                            }
                        } catch (Settings.SettingNotFoundException e) {
                        }
                        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true, brightnessContentObserver);
                        break;
                    case sound:
                        sound_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        sound_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent soundIntent = new Intent(context, TogglerActions.class);
                        soundIntent.setAction(SOUND_CLICK);

                        PendingIntent soundPendingIntent = PendingIntent.getBroadcast(context, 0, soundIntent, 0);

                        remoteViews.setOnClickPendingIntent(sound_image, soundPendingIntent);

                        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        remoteViews.setImageViewResource(sound_icon, ringerImages.get(ringerModes.indexOf(audiomanager.getRingerMode())));

                        break;
                    case flashlight:
                        flashlight_image = context.getResources().getIdentifier(images.get(i) + "_image", "id", context.getPackageName());
                        flashlight_icon = context.getResources().getIdentifier(images.get(i) + "_icon", "id", context.getPackageName());

                        Intent flashlightIntent = new Intent(context, TogglerActions.class);
                        flashlightIntent.setAction(FLASHLIGHT_CLICK);

                        PendingIntent flashlightPendingIntent = PendingIntent.getBroadcast(context, 0, flashlightIntent, 0);

                        remoteViews.setOnClickPendingIntent(flashlight_image, flashlightPendingIntent);
                        remoteViews.setImageViewResource(flashlight_icon, R.drawable.icon_flashlight_off);
                        break;
                }
            }
        } else if (WIFI_CLICK.equals(action)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_off);
                } else {
                    wifiManager.setWifiEnabled(true);
                    remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_on);
                }
            }
        } else if (GPS_CLICK.equals(action)) {
            Intent gpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            gpsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(gpsIntent);
        } else if (BLUETOOTH_CLICK.equals(action)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null)
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                    remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_off);
                } else {
                    bluetoothAdapter.enable();
                    remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_on);
                }
        } else if (ROTATION_CLICK.equals(action)) {
            if (android.provider.Settings.System.getInt(context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 0);
                remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_off);
            } else {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION, 1);
                remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_on);
            }
        } else if (THREE_G_CLICK.equals(action)) {
            try {
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (manager != null) {
                    Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    dataMtd.setAccessible(true);

                    if (manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting()) {
                        dataMtd.invoke(manager, false);
                        remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_off);
                    } else {
                        dataMtd.invoke(manager, true);
                        remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_on);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)) {
            if (((LocationManager) context.getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER))
                remoteViews.setImageViewResource(gps_icon, R.drawable.icon_gps_on);
            else
                remoteViews.setImageViewResource(gps_icon, R.drawable.icon_gps_off);
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                if (wifiManager.isWifiEnabled())
                    remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_on);
                else
                    remoteViews.setImageViewResource(wifi_icon, R.drawable.icon_wifi_off);
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                if (manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting())
                    remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_on);
                else
                    remoteViews.setImageViewResource(three_g_icon, R.drawable.icon_three_g_off);
            }
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled())
                    remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_on);
                else
                    remoteViews.setImageViewResource(bluetooth_icon, R.drawable.icon_bluetooth_off);
            }
        } else if (ROTATION_SWITCHED.equals(action)) {
            if (android.provider.Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
                remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_on);
            else
                remoteViews.setImageViewResource(rotate_icon, R.drawable.icon_rotation_off);
        } else if (SOUND_CLICK.equals(action)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int index = (ringerModes.indexOf(audioManager.getRingerMode()) + 1) % ringerModes.size();
            audioManager.setRingerMode(ringerModes.get(index));
            remoteViews.setImageViewResource(sound_icon, ringerImages.get(index));
        } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            remoteViews.setImageViewResource(sound_icon, ringerImages.get(ringerModes.indexOf(audioManager.getRingerMode())));
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            	v.vibrate(500);
            }
        } else if (BRIGHTNESS_CLICK.equals(action)) {
            switch (current_brightness_image) {
                case R.drawable.icon_brightness_85:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 170);
                    current_brightness_image = R.drawable.icon_brightness_170;
                    remoteViews.setImageViewResource(brightness_icon, current_brightness_image);
                    break;
                case R.drawable.icon_brightness_170:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
                    current_brightness_image = R.drawable.icon_brightness_255;
                    remoteViews.setImageViewResource(brightness_icon, current_brightness_image);
                    break;
                case R.drawable.icon_brightness_255:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    current_brightness_image = R.drawable.icon_brightness_auto;
                    remoteViews.setImageViewResource(brightness_icon, current_brightness_image);
                    break;
                case R.drawable.icon_brightness_auto:
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 85);
                    current_brightness_image = R.drawable.icon_brightness_85;
                    remoteViews.setImageViewResource(brightness_icon, current_brightness_image);
                    break;
            }
        } else if (BRIGHTNESS_MODE_SWITCHED.equals(action)) {
            if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == 0)
                current_brightness_image = R.drawable.icon_brightness_85;
            else
                current_brightness_image = R.drawable.icon_brightness_auto;
            remoteViews.setImageViewResource(brightness_icon, current_brightness_image);
        } else if (FLASHLIGHT_CLICK.equals(action)) {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                if (cam == null)
                    cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cam.setParameters(p);
                    cam.stopPreview();
                    remoteViews.setImageViewResource(flashlight_icon, R.drawable.icon_flashlight_off);
                } else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();
                    remoteViews.setImageViewResource(flashlight_icon, R.drawable.icon_flashlight_on);
                }
            } else
                Toast.makeText(context, "Your device has no camera flash", Toast.LENGTH_SHORT).show();
        } else if (SETTINGS_CLICK.equals(action)) {
        	Intent intent_settings = new Intent(context, TogglersConfigurationActivity.class);
        	intent_settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent_settings);
        }

        ComponentName thisWidget = new ComponentName(context, TogglerActions.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);
        super.onReceive(context, intent);
    }
}

