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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.swissbit.homeautomation.activity.DeviceActivity;
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
 * This AsyncTask handles the retrieval of list of devices of a RaspberryPi.
 */
public class RetrieveDeviceListAsync extends AsyncTask {

    /**
     * Subscription response
     */
    boolean subscriptionResponse;

    /**
     * Node id of the device.
     */
    private int deviceNodeId;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     * The id of RaspberryPi
     */
    private String raspberryId;

    /**
     * A lock object for thread synchronisation
     */
    private Lock lock;

    /**
     * A condition object used by locks for thread synchronisation
     */
    private Condition condition;

    /**
     * Progress Dialog object to display progress
     */
    private ProgressDialog progressDialog;

    /**
     * The secure element ID of the SD card
     */
    private CardAPI secureElementAccess;

    /**
     * Constructor
     */
    public RetrieveDeviceListAsync(String raspberryId) {
        this.raspberryId = raspberryId;
        subscriptionResponse = false;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    /**
     * Start the progress dialog
     */
    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(ActivityContexts.getDeviceActivityContext(), "Retrieving Device List",
                "Please Wait", true);
        secureElementAccess = new CardAPI(ActivityContexts.getDeviceActivityContext());
    }

    /**
     * Handles the subscription and publish event for device list retrieval
     */
    @Override
    protected Object doInBackground(Object[] params) {
        final IKuraMQTTClient client = MQTTFactory.getClient();
        applicationDb = DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getDeviceActivityContext());
        boolean status = false;


        if (!client.isConnected())
            status = client.connect();

        status = client.isConnected();

        String[] topicData = MQTTFactory.getTopicToSubscribe(TopicsConstants.SWITCH_ON_OFF_LIST_STATUS_SUB);
        String requestId = topicData[1];
        Log.d("RequestID", requestId);
        final String topic = topicData[0];

        //Subscribe to the topic. After publish, the response will be handles here.
        if (status) {
            client.subscribe(topic, new MessageListener() {
                @Override
                public void processMessage(KuraPayload kuraPayload) {
                    try {

                        int status = (int) kuraPayload.getMetric("response.code");

                        if (status == 200) {
                            Log.d("Response", "success");
                            deviceNodeId = Integer.valueOf((String) kuraPayload.getMetric("node.id_0"));
                            Log.d("HaspMap", "" + deviceNodeId);
                            Log.d("Device", "" + applicationDb.checkDeviceById(deviceNodeId));
                            if (!applicationDb.checkDeviceById(deviceNodeId)) {
                                applicationDb.insertDevice(deviceNodeId, raspberryId, null, null, "false");
                                Log.d("Device", "Inserted");
                            }
                            publishProgress();

                            Log.d("Notify", "Before");

                            if (lock.tryLock()) {
                                try {
                                    Log.d("Notify", "After");
                                    condition.signal();
                                }
                                finally {
                                    lock.unlock();
                                }
                            }
                            subscriptionResponse = true;

                        } else {
                            Log.d("Response", "Failed");
                            subscriptionResponse = false;
                        }
                        Log.d("Inside onProcess", "" + subscriptionResponse);
                    } catch (Exception e) {
                        Log.e("Kura MQTT Exception", e.getCause().getMessage());
                    }

                }
            });

            //Generate the payload
            KuraPayload payload = MQTTFactory.generatePayload("", requestId);

            //Encrypt any string. Here its "Raspberry".
            String encryptedString = secureElementAccess.encryptMsgWithID(MQTTFactory.getSecureElementId(),"Raspberry");
            payload.addMetric("encVal", encryptedString);

            //Publish
            MQTTFactory.getClient().publish(MQTTFactory.getTopicToPublish(TopicsConstants.RETRIEVE_DEVICE_LIST_PUB), payload);
            Log.d("Topic Published", MQTTFactory.getTopicToPublish(TopicsConstants.RETRIEVE_DEVICE_LIST_PUB));
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
        return null;
    }

    /**
     * Check if the response has been received. If not, display failure message.
     */
    @Override
    protected void onPostExecute(Object o) {
        if(subscriptionResponse) {
            DeviceActivity deviceActivity = (DeviceActivity) ActivityContexts.getDeviceActivityContext();
            deviceActivity.addToListView();
        }
        Log.d("Inside onPost", "" + subscriptionResponse);
        if (!subscriptionResponse){
            progressDialog.dismiss();
            Toast.makeText(ActivityContexts.getMainActivityContext(), "Failed to retrieve device list! Please Try again", Toast.LENGTH_LONG).show();
        }
        cancel(true);
    }

    @Override
    protected void onCancelled() {
        Log.d("Inside onCancelled", "" + subscriptionResponse);
    }

    /**
     *Dismiss the progress bar
     */
    @Override
    public void onProgressUpdate(Object[] values) {
        progressDialog.dismiss();
        Log.d("Onprogess", "reached");

    }
}
