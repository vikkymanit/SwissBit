/**
 * ****************************************************************************
 * Copyright (C) 2015 - Manit Kumar <vikky_manit@yahoo.co.in>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.swissbit.homeautomation.asyncTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.swissbit.homeautomation.activity.MainActivity;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.utils.EncryptionFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.adapter.MessageListener;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

/**
 * This AsyncTask handles the access revocation of secure SD card.
 */
public class PermissionRevocationAsync extends AsyncTask {

    /**
     *Subscription response
     */
    boolean subscriptionResponse;

    /**
     * The object to access the secure element for the SD card
     */
    private CardAPI secureElementAccess;

    /**
     *Constructor
     */
    public PermissionRevocationAsync(Context context) {
        secureElementAccess = new CardAPI(ActivityContexts.getMainActivityContext());
        subscriptionResponse = false;
    }

    /**
     *Handles the subscription event for access revocation of secure SD card
     */
    @Override
    protected Object doInBackground(Object[] params) {

        boolean status = false;
        IKuraMQTTClient client = MQTTFactory.getClient();

        if (!client.isConnected()) {
            status = client.connect();
        }

        status = client.isConnected();

        //Subscribe to the topic. Revoke the access and delete all app data when revocation command is received.
        Log.d("Kura MQTT Connect AR", Boolean.toString(status));
        if (status) {
            Log.d("AccessRevoke", MQTTFactory.getTopicToSubscribe(TopicsConstants.ACCESS_REVOCATION_SUB)[0]);
            client.subscribe(MQTTFactory.getTopicToSubscribe(TopicsConstants.ACCESS_REVOCATION_SUB)[0], new MessageListener() {
                @Override
                public void processMessage(KuraPayload kuraPayload) {
                    if (kuraPayload != null) {
                        if((boolean) kuraPayload.getMetric("revokedStatus")) {
                            Log.d("AccessRevoked", "Revoked");
                            ApplicationDb applicationDb = DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getCurrentActivityContext());
                            applicationDb.resetData();

                            secureElementAccess.setDisabled();
                            publishProgress();
                        }

                    }
                }
            });
        }

        return null;
    }

    /**
     *Display the access revocked dialog message and terminate the application
     */
    @Override
    public void onProgressUpdate(Object[] values) {
        AlertDialog alertDialog = new AlertDialog.Builder(ActivityContexts.getCurrentActivityContext()).create();
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("Your Access has been revoked. Application will no longer function");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                });
        alertDialog.show();

    }
}
