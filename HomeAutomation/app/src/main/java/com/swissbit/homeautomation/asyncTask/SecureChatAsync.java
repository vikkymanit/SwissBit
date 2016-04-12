/**
 * ****************************************************************************
 * Copyright (C) 2015 - Gaurav Kumar Srivastava
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.utils.Message;
import com.swissbit.homeautomation.utils.MessageListSingleton;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.MessagesListAdapter;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.adapter.MessageListener;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles device's on/off commands
 */
public class SecureChatAsync extends AsyncTask {

    /**
     * The Chat messages list adapter
     */
    private MessagesListAdapter adapter;

    /**
     * The list of messages in the conversation
     */
    private List<Message> listMessages;

    /**
     * The view to display the messages
     */
    private ListView listViewMessages;

    /**
     *Incoming Message
     */
    String incomingMsg;

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
    public SecureChatAsync() {
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
        //progressDialog = ProgressDialog.show(ActivityContexts.getDeviceActivityContext(), "Executing Command",
          //      "Please Wait", true);
    }

    /**
     *Handles the subscription and publish event for device command
     */
    @Override
    protected Object doInBackground(Object[] params) {
        IKuraMQTTClient client = MQTTFactory.getClient();
        boolean status = false;

        while(true) {

            if (!client.isConnected())
                status = client.connect();

        status = client.isConnected();

        String topic = TopicsConstants.SECURE_CHAT;

            //Subscribe to the topic. After publish, the response will be handles here.

            if (status)
                client.subscribe(topic, new MessageListener() {
                    @Override
                    public void processMessage(KuraPayload kuraPayload) {
                        try {
                                Log.d("Response", "success");
                                subscriptionResponse = true;
                                incomingMsg = (String) kuraPayload.getMetric("message");
                                if(incomingMsg != "" && incomingMsg != null)
                                    publishProgress();

                            Log.d("Inside onProcess", "" + subscriptionResponse);
                        } catch (Exception e) {
                            Log.e("Kura MQTT Exception", e.getCause().getMessage());
                        }

                    }
                });

        }

    }

    /**
     *Check if the response has been received. If not, display failure message.
     */
    @Override
    protected void onPostExecute(Object o) {
        Log.d("Inside onPost", "" + subscriptionResponse);
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

        if(subscriptionResponse) {
            View rootView = ((Activity) ActivityContexts.getChatActivityContext()).getWindow().getDecorView().findViewById(android.R.id.content);
            ListView listViewMessages = (ListView) rootView.findViewById(R.id.list_view_messages);
            listMessages = MessageListSingleton.getInstance().getListMessages();

            adapter = new MessagesListAdapter(ActivityContexts.getChatActivityContext(), listMessages);
            listViewMessages.setAdapter(adapter);

            secureElementAccess = new CardAPI(ActivityContexts.getChatActivityContext());
            String decMsg[] = secureElementAccess.decryptMsgWithID(incomingMsg);
            Log.d("Decrypted Message", decMsg[1]);

            if(!decMsg[1].contains("Error:")) {
                decMsg[1] = decMsg[1].substring(0, decMsg[1].length() - 1);

                String senderName = decMsg[1].split(";;")[0];
                String message = decMsg[1].split(";;")[1];

                Message m = new Message(senderName, message, false);

                listMessages.add(m);
                adapter.notifyDataSetChanged();
                playBeep();

            }

        }
    }

    /**
     * Plays device's default notification sound
     * */
    public void playBeep() {

        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(ActivityContexts.getChatActivityContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
