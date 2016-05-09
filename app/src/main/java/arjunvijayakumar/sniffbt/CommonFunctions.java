package arjunvijayakumar.sniffbt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

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
}
