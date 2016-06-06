package millennia.sniffbt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.view.View;

import com.google.gson.Gson;

public class CommonFunctions {

    /**
     * Method to put application in sleep for specified number of seconds
     * @param intSleepTimeInSeconds - The {@link int} number of seconds
     */
    public void sleepForNSeconds(int intSleepTimeInSeconds){
        try {
            Thread.sleep(intSleepTimeInSeconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to display a notification
     * @param title - The {@link String} title for the notification
     * @param text - The {@link String} text for the notification
     */
    public void displayNotification(Context context, String title, String text, Class<?> cls) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_bt_on)
                        .setContentTitle(title)
                        .setContentText(text);
        Intent resultIntent = new Intent(context, cls);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * Method to store a complex object in the Shared Preferences class
     * @param  mPref - the {@link SharedPreferences} object
     * @param strReferenceName - The {@link String} reference name which will be used to retrieve the object
     * @param obj - The {@link Object}
     */
    public void setSharedPreferences(SharedPreferences mPref, String strReferenceName, Object obj) {
        SharedPreferences.Editor prefEditor = mPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        prefEditor.putString(strReferenceName, json);
        prefEditor.commit();
    }

    /**
     * Method to retireve a saved Shared Preferences object
     * @param  mPref - the {@link SharedPreferences} object
     * @param strReferenceName - The {@link String} reference name
     * @return - The {@link Object}
     */
    public Object getSharedPreferences(SharedPreferences mPref, String strReferenceName, Class<?> objClass) {
        Gson gson = new Gson();
        String json = mPref.getString(strReferenceName, "");
        return gson.fromJson(json, objClass);
    }

    /**
     * Method to display a SnackBar message
     * @param strMsg - The {@link String} message to be displayed
     */
    public void showSnackBar(View view, String strMsg, int intTime) {
        Snackbar snackbar = Snackbar.make(view, strMsg, intTime);
        snackbar.show();
    }

}
