package arjunvijayakumar.sniffbt;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class ListenBTIntentService extends IntentService {

    public ListenBTIntentService() {
        super("ListenBTIntentService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        final String TAG = "Smell BT Intent Service";
        Log.i(TAG, "Inside Intent Service...");

        Intent localIntent = new Intent(getString(R.string.intent_Broadcast));
        Log.i(TAG, "Sending Broadcast...");
        sendBroadcast(localIntent);
    }
}
