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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.adapter.MessageListener;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles device's on/off commands
 */
public class DeviceCmdAsync extends AsyncTask {

    /**
     *Subscription response
     */
    boolean subscriptionResponse;

    /**
     *Command. Either on or off.
     */
    private String cmd;

    /**
     * A lock object for thread synchronisation
     */
    private Lock lock;

    /**
     * A condition object used by locks for thread synchronisation
     */
    private Condition condition;

    /**
     *Node id of the device.
     */
    private int deviceNodeId;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     *Progress Dialog object to display progress
     */
    private ProgressDialog progressDialog;

    /**
     * The object to access the secure element for the SD card
     */
    private CardAPI secureElementAccess;

    /**
     *Constructor
     */
    public DeviceCmdAsync(String cmd, int deviceNodeId, String raspberryId) {
        this.cmd = cmd;
        this.deviceNodeId = deviceNodeId;
        applicationDb = DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getDeviceActivityContext());
        secureElementAccess = new CardAPI(ActivityContexts.getMainActivityContext());
        subscriptionResponse = false;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    /**
     *Start the progress Dialog
     */
    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(ActivityContexts.getDeviceActivityContext(), "Executing Command",
          "Please Wait", true);
    }

    /**
     *Handles the subscription and publish event for device command
     */
    @Override
    protected Object doInBackground(Object[] params) {
        IKuraMQTTClient client = MQTTFactory.getClient();
        boolean status = false;

        if (!client.isConnected())
            status = client.connect();

        status = client.isConnected();

        String topic = null;

        String[] topicData = MQTTFactory.getTopicToSubscribe(TopicsConstants.SWITCH_ON_OFF_LIST_STATUS_SUB);
        String requestId = topicData[1];
        Log.d("RequestID", requestId);
        topic = topicData[0];

        //Subscribe to the topic. After publish, the response will be handles here.
        if (status)
            client.subscribe(topic, new MessageListener() {
                @Override
                public void processMessage(KuraPayload kuraPayload) {
                    try {
                        int status = (int) kuraPayload.getMetric("response.code");

                        if (status == 200) {
                            Log.d("Response", "success");
                            subscriptionResponse = true;
                            if (cmd.equals("on"))
                                applicationDb.updateDeviceStatus("true", deviceNodeId);
                            else
                                applicationDb.updateDeviceStatus("false", deviceNodeId);
                        } else {
                            Log.d("Response", "Failed");
                            subscriptionResponse = false;

                        }

                        if (lock.tryLock()) {
                            try {
                                Log.d("Notify", "After");
                                condition.signal();
                            }
                            finally {
                                lock.unlock();
                            }
                        }

                        Log.d("Inside onProcess", "" + subscriptionResponse);
                    } catch (Exception e) {
                        Log.e("Kura MQTT Exception", e.getCause().getMessage());
                    }

                }
            });

        //Generate the payload
        KuraPayload payload = MQTTFactory.generatePayload("", requestId);

        //Encrypt the node id of the device
        String encryptedDeviceNodeId = secureElementAccess.encryptMsgWithID(MQTTFactory.getSecureElementId(),Integer.toString(deviceNodeId));

        payload.addMetric("nodeId",deviceNodeId);
        payload.addMetric("encVal", encryptedDeviceNodeId);

        //Publish based on/off command
        if (cmd.equals("on")) {
            MQTTFactory.getClient().publish(MQTTFactory.getTopicToPublish(TopicsConstants.SWITCH_ON_PUB), payload);
            Log.d("Switch", "onpresses" + MQTTFactory.getTopicToPublish(TopicsConstants.SWITCH_ON_PUB));
        } else if (cmd.equals("off")) {
            MQTTFactory.getClient().publish(MQTTFactory.getTopicToPublish(TopicsConstants.SWITCH_OFF_PUB), payload);
            Log.d("Switch", "offpresses");
        }

        //Wait for sometime for the response
        if (lock.tryLock()) {
            try {
                Log.d("Notify", "Before");
                condition.await(20, TimeUnit.SECONDS);
                Log.d("Notify", "After");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        publishProgress();

        return null;
    }

    /**
     *Check if the response has been received. If not, display failure message.
     */
    @Override
    protected void onPostExecute(Object o) {
        Log.d("Inside onPost", "" + subscriptionResponse);
        if (!subscriptionResponse) {
            Toast.makeText(ActivityContexts.getDeviceActivityContext(), "Device Command Failed! Try again", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
        cancel(true);
    }

    @Override
    protected void onCancelled() {
        Log.d("Inside onCancelled", "" + subscriptionResponse);
    }

    /**
     *Change the image of the device based on the successful execution of on/off command.
     */
    @Override
    public void onProgressUpdate(Object[] values) {
        progressDialog.dismiss();

        if(subscriptionResponse) {
            View rootView = ((Activity) ActivityContexts.getDeviceActivityContext()).getWindow().getDecorView().findViewById(android.R.id.content);
            ImageView imageDevice = (ImageView) rootView.findViewById(R.id.imgDevice);
            if (cmd.equals("on")) {
                imageDevice.setImageResource(R.drawable.socketswitchon);
            } else {
                imageDevice.setImageResource(R.drawable.socketswitchoff);
            }
        }
    }
}
