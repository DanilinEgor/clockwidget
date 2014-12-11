package com.example.clockwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by egor on 29.04.14.
 */
public class TogglersConfigurationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String[] items = new String[]{"Wi-Fi", "GPS", "Bluetooth", "Auto-rotate screen", "Mobile data", "Brightness", "Sound", "Flashlight"};
        final boolean[] on = new boolean[items.length];
        final int[] num = {0};

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            boolean[] on1 = extras.getBooleanArray("on");
            if (on1 != null)
                for (int i = 0; i < on1.length; ++i) {
                    if (on1[i]) ++num[0];
                    on[i] = on1[i];
                }

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_CANCELED);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Choose togglers");
            alert.setCancelable(false);
            alert.setMultiChoiceItems(items, on, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    if (b)
                        ++num[0];
                    else
                        --num[0];
                    on[i] = b;
                    if (num[0] > 5) {
                        Toast.makeText(TogglersConfigurationActivity.this, "Select up to 5 items", Toast.LENGTH_SHORT).show();
                        ((AlertDialog) dialogInterface).getListView().setItemChecked(i, false);
                        on[i] = false;
                        num[0] = 5;
                    }
                }
            });



            final int finalMAppWidgetId = mAppWidgetId;
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(TogglersConfigurationActivity.this, TogglerActions.class);
                    intent.setAction("TogglerActions.APPWIDGET_ENABLED");
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, finalMAppWidgetId);
                    intent.putExtra("on", on);
                    sendBroadcast(intent);

                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, finalMAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                    TogglersConfigurationActivity.this.finish();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    TogglersConfigurationActivity.this.finish();
                }
            });

            alert.show();
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
