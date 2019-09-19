package com.defold.push;

import java.io.PrintStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushDispatchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrintStream os = null;
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String payload = extras.getString("payload");
                int uid = extras.getInt("uid");
                boolean wasActivated = (extras.getByte("wasActivated") == 1);
                boolean remoteOrigin = (extras.getByte("remote") == 1);

                if (Push.getInstance().hasListener()) {
                    if (remoteOrigin) {
                        Push.getInstance().onRemotePush(payload, wasActivated);
                    } else {
                        Push.getInstance().onLocalPush(payload, uid, wasActivated);
                    }
                } else {
                    // need to save this to disk until a listener is set
                    if (remoteOrigin) {
                        os = new PrintStream(openFileOutput(Push.SAVED_PUSH_MESSAGE_NAME, MODE_PRIVATE));
                        os.println(wasActivated);
                        os.println(payload);
                    } else {
                        os = new PrintStream(openFileOutput(Push.SAVED_LOCAL_MESSAGE_NAME, MODE_PRIVATE));
                        os.println(uid);
                        os.println(wasActivated);
                        os.println(payload);
                    }

                    // Start activity with intent
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            } else {
                Log.e(Push.TAG, "Unable to queue message. extras is null");
            }
        } catch (Throwable e) {
            Log.e(Push.TAG, "Failed to write push message to disk", e);
        } finally {
            if (os != null) {
                os.close();
            }
        }

        finish();
    }
}
