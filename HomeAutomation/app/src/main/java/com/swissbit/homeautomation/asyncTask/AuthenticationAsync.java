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
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.swissbit.homeautomation.activity.MainActivity;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.EncryptionFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.homeautomation.utils.WSConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.adapter.MessageListener;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

import org.apache.http.Header;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This AsyncTask handles the authentication of RaspberryPi.
 */
public class AuthenticationAsync extends AsyncTask {

    /**
     *MainActivity context object
     */
    private MainActivity mainActivity;

    /**
     *RaspberryPi Id
     */
    private String rid;

    /**
     *Subscription response
     */
    boolean subscriptionResponse;

    /**
     *payload for the publish
     */
    private KuraPayload payload;

    /**
     *Dialog message object
     */
    private String dialogMessage;

    /**
     *MainActivity context object
     */
    private Context mainActivityContext;

    /**
     * A lock object for thread synchronisation
     */
    private Lock lock;

    /**
     * A condition object used by locks for thread synchronisation
     */
    private Condition condition;

    /**
     *Progress Dialog object to display progress
     */
    private ProgressDialog progressDialog;

    /**
     *Constructor
     */
    public AuthenticationAsync(Context context, final String rid) {
        this.mainActivity = (MainActivity)context;
        this.mainActivityContext = context;
        this.rid = rid;
        subscriptionResponse = false;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    /**
     *Show the dialog message after the RaspberryPi authentication
     */
    public void showDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(mainActivityContext).create();
        alertDialog.setTitle("Information");
        alertDialog.setMessage(dialogMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(subscriptionResponse)
                            mainActivity.checkRaspberryId(rid);
                        dialog.dismiss();
                        cancel(true);
                    }
                });
        alertDialog.show();
    }

    /**
     * Show the progress dialog on authentication with server
     */
    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(ActivityContexts.getMainActivityContext(), "Authenticating with Server",
                "Please Wait", true);
    }

    /**
     *Handles the subscription and publish event for RaspberryPi Authentication
     */
    @Override
    protected Object doInBackground(Object[] params) {
        IKuraMQTTClient client = MQTTFactory.getClient();
        boolean status = false;
        String requestId = null;
        String topic = null;

        if (!client.isConnected())
            status = client.connect();

        status = client.isConnected();

        String[] topicData = MQTTFactory.getTopicToSubscribe(TopicsConstants.RASPBERRY_AUTH_SUB);
        requestId = topicData[1];
        topic = topicData[0];

        Log.d("Kura MQTTTopic", topic);
        Log.d("Kura requestId", requestId);

        //Subscribe to the topic. After publish, the response will be handles here.
        if (status)
            client.subscribe(topic, new MessageListener() {
                @Override
                public void processMessage(KuraPayload kuraPayload) {
                    try {

                        String metricData = String.valueOf(kuraPayload.getMetric("data"));
                        if (metricData.isEmpty()) {
                            Log.d("Metrics", "" + kuraPayload.metrics());
                            subscriptionResponse = false;
                        } else {
                            Log.d("Metrics", "" + kuraPayload.metrics());
                            subscriptionResponse = true;
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
                        Log.e("Kura MQTT", e.getCause().getMessage());
                    }

                }
            });


        if (!status)
            MQTTFactory.getClient().connect();

        Log.d("AuthTopic", "" + MQTTFactory.getTopicToPublish(TopicsConstants.RASPBERRY_AUTH_PUB));
        Log.d("EncryptAsyncFactory", "" + EncryptionFactory.getEncryptedString());

        //Generate the payload
        payload = MQTTFactory.generatePayload(EncryptionFactory.getEncryptedString(), requestId);

        //Publish
        if (status)
            MQTTFactory.getClient().publish(MQTTFactory.getTopicToPublish(TopicsConstants.RASPBERRY_AUTH_PUB), payload);

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

    @Override
    protected void onCancelled() {
        Log.d("Inside onCancelled", "" + subscriptionResponse);
    }

    /**
     *Checks if the response has been received after publishing.
     * If yes, then makes a call to the webservice to update that the RaspberryPi had been added.
     * This is for swissbit admin to know that the RaspberryPi is activated and added.
     */
    @Override
    protected void onPostExecute(Object o) {
        Log.d("Inside onPost", "" + subscriptionResponse);

        if (subscriptionResponse) {
            Log.d("Inside onPostWS", WSConstants.ADD_RPI_WS + MQTTFactory.getRaspberryPiById());
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.get(WSConstants.ADD_RPI_WS + MQTTFactory.getRaspberryPiById(), new AsyncHttpResponseHandler() {

                //On successful authentication of RaspberryPi
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("DEBUG AUTHASYNC", "INSIDE SUCCESS");
                    payload.addMetric("secure_element", MQTTFactory.getMobileClientSecureElementId());
                    payload.addMetric("encVal",EncryptionFactory.getEncryptedString());

                    MQTTFactory.getClient().publish(MQTTFactory.getTopicToPublish(TopicsConstants.SURVEILLANCE), payload);
                    Toast.makeText(ActivityContexts.getMainActivityContext(), "RaspberryPi Validated", Toast.LENGTH_LONG).show();
                    dialogMessage = "RaspberryPi Validated";
                    showDialog();
                }

                //On unsuccessful authentication of RaspberryPi
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("DEBUG AUTHASYNC", "INSIDE FAILURE");
                    Toast.makeText(ActivityContexts.getMainActivityContext(), "RaspberryPi Registration Unsuccessful. Please try Again", Toast.LENGTH_LONG).show();
                    dialogMessage = "RaspberryPi Registration Unsuccessful. Please try Again";
                    subscriptionResponse = false;
                    showDialog();
                }
            });

        } else {
            Log.d("Auth Debug", "Failure");
            Toast.makeText(ActivityContexts.getMainActivityContext(), "RaspberryPi Registration Unsuccessful. Please try Again", Toast.LENGTH_LONG).show();
            dialogMessage = "RaspberryPi Registration Unsuccessful. Please try Again";
            subscriptionResponse = false;
            showDialog();
        }
        progressDialog.dismiss();
        cancel(true);
    }
}
