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

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.adapter.MessageListener;
import com.swissbit.mqtt.client.message.KuraPayload;

/**
 * This AsyncTask handles the heart beat of RaspberryPi.
 */
public class HeartBeatAsync extends AsyncTask {

    /**
     *Handles the subscription event for heart beat of the RaspberryPi
     */
    @Override
    protected Object doInBackground(Object[] params) {
        boolean status = false;
        IKuraMQTTClient client = MQTTFactory.getClient();
        Log.d("Kura MQTT Client ", "" + client);

        if (!client.isConnected()) {
            status = client.connect();
        }

        status = client.isConnected();

        Log.d("Kura MQTT HB", Boolean.toString(status));

        //Subscribe to the topic.
        if (status) {
            Log.d("HEARTBEATTOPIC", MQTTFactory.getTopicToSubscribe(TopicsConstants.HEARTBEAT)[0]);
            client.subscribe(MQTTFactory.getTopicToSubscribe(TopicsConstants.HEARTBEAT)[0], new MessageListener() {
                @Override
                public void processMessage(KuraPayload kuraPayload) {
                    if (kuraPayload != null) {
                        Log.d("Kura HeartBeat", "Raspberry Alive...");
                        publishProgress();
                    }
                }
            });
        }
        return null;
    }

    /**
     *Change the image of the Raspberry Status based when heart beat is received
     */
    @Override
    public void onProgressUpdate(Object[] values) {
        View rootView = ((Activity) ActivityContexts.getMainActivityContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        ImageView imgStatus = (ImageView) rootView.findViewById(R.id.imgStatus);
        imgStatus.setImageResource(R.drawable.btnon);
        imgStatus.setTag(R.drawable.btnon);
    }
}
