package com.dpellier.x_per_day;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

public class WidgetProvider extends AppWidgetProvider {

    // Widget actions
    public static String IS_DONE = "IS_DONE";
    public static String NEW_DAY = "NEW_DAY";
    public static String PLUS = "PLUS";
    public static String MINUS = "MINUS";

    // Widget values
    public static int COUNTER = 0;
    public static int THRESHOLD = 5;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateCount(context);
    }

    /**
     * Manage widget possibles actions
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (IS_DONE.equals(intent.getAction())) {
            COUNTER -= THRESHOLD;
            if (COUNTER < 0) {
                COUNTER = 0;
            }
            updateCount(context);
        }

        else if (NEW_DAY.equals(intent.getAction())) {
            COUNTER += THRESHOLD;
            updateCount(context);
        }

        else if (PLUS.equals(intent.getAction())) {
            THRESHOLD += 1;
            updateCount(context);
        }

        else if (MINUS.equals(intent.getAction())) {
            THRESHOLD -= 1;
            updateCount(context);
        }

        super.onReceive(context, intent);
    }

    /**
     * On first widget use, we set an alarm so that each day the its value will be updated
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        int[] allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, newIntent(context, WidgetProvider.NEW_DAY, allWidgetIds));
    }

    /**
     * Delete the alarm on widget disabling
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        int[] allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(newIntent(context, WidgetProvider.NEW_DAY, allWidgetIds));
    }

    /**
     * Update the widget
     * @param context
     */
    private void updateCount(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            remoteViews.setTextViewText(R.id.count, String.valueOf(COUNTER));
            remoteViews.setTextViewText(R.id.threshold, String.valueOf(THRESHOLD));

            // Register onClickListener
            remoteViews.setOnClickPendingIntent(R.id.done, newIntent(context, WidgetProvider.IS_DONE, allWidgetIds));
            remoteViews.setOnClickPendingIntent(R.id.threshold_plus, newIntent(context, WidgetProvider.PLUS, allWidgetIds));
            remoteViews.setOnClickPendingIntent(R.id.threshold_minus, newIntent(context, WidgetProvider.MINUS, allWidgetIds));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    /**
     * Create an Intent with given specific action
     * @param context
     * @param action
     * @param allWidgetIds
     * @return PendingIntent
     */
    private PendingIntent newIntent(Context context, String action, int[] allWidgetIds) {
        Intent intent = new Intent(context, WidgetProvider.class);

        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
