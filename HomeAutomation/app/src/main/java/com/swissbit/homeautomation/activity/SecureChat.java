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

package com.swissbit.homeautomation.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import java.util.List;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.swissbit.homeautomation.asyncTask.SecureChatAsync;
import com.swissbit.homeautomation.utils.MessageListSingleton;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.MessagesListAdapter;
import com.swissbit.homeautomation.utils.Message;


import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.utils.TopicsConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

/**
 * This class handles the secure chat functionality.
 */

public class SecureChat extends ActionBarActivity {

    /**
     * The object to access the secure element for the SD card
     */
    private CardAPI secureElementAccess;

    /**
     * The secure element ID of the SD card
     */
    private String secureElementId;

    /**
     * Mqtt client
     */
    IKuraMQTTClient client;

    /**
     * Part of subscribe topic for secure chat
     */
    String topic = TopicsConstants.SECURE_CHAT;

    /**
     * The name of the conversation initiator
     */
    private String myName;

    /**
     * The Id of the conversation initiator
     */
    private String recId;

    /**
     * The button to send the message
     */
    private Button btnSend;

    /**
     * The input message
     */
    private EditText inputMsg;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_chat);

        //Save the context of DeviceActivity for later usage in other classes
        ActivityContexts.setChatActivityContext(this);

        Intent intent =  getIntent();
        myName = intent.getStringExtra("name");
        recId  = intent.getStringExtra("recId");

        secureElementAccess = new CardAPI(getApplicationContext());
        secureElementId = secureElementAccess.getMyId();
        Log.d("SecureIDB: ", secureElementId);
        //Based on Radio Button Selected
        secureElementId = recId;
        Log.d("SecureIDA: ", secureElementId);

        //To check for network availability
        checkNetworkAvailability();

        //To check if access has been revoked
        checkAccessRevoked();

        btnSend = (Button) findViewById(R.id.btnSend);
        inputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);
        client = MQTTFactory.getClient();

        SecureChatAsync secureChatAsync = new SecureChatAsync();
        secureChatAsync.execute();

        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean status = false;
                // Sending message to MQTT Message Broker
                if (!client.isConnected())
                    status = client.connect();

                status = client.isConnected();
                String message = secureElementAccess.encryptMsgWithID(secureElementId, myName + ";;" + inputMsg.getText().toString());

                KuraPayload payload = MQTTFactory.generatePayload("SecureChat", "1033");
                payload.addMetric("message", message);

                MQTTFactory.getClient().publish(topic, payload);

                parseMessageOutgoing(inputMsg.getText().toString());

                // Clearing the input filed once message was sent
                inputMsg.setText("");
            }
        });

        listMessages = MessageListSingleton.getInstance().getListMessages();

        adapter = new MessagesListAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_secure_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Appending message to list view
     * */
    private void appendMessage(final Message m) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                listMessages.add(m);

                adapter.notifyDataSetChanged();

                // Playing device's notification
                playBeep();
            }
        });
    }

    /**
     * Plays device's default notification sound
     * */
    public void playBeep() {
        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the outgoing message
     *
     */
    private void parseMessageOutgoing(final String msg) {
        boolean isSelf = true;

        Message m = new Message(myName, msg, isSelf);

        // Appending the message to chat list
        appendMessage(m);
    }

    /**
     * Used to check if the access has been previously revoked when the application starts.
     * Post this check ask for the secure code only for the first time.
     * Then, adds the RaspberryPi to the listview
     */
    public void checkAccessRevoked(){
        if("029000".equals(secureElementAccess.getEnabled().toString())) {
            Log.d("Inside Alert","Alert");
            Context context = SecureChat.this;
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Warning!");
            alertDialog.setMessage("Your Access has been revoked. Application will no longer function");
            alertDialog.setCancelable(false);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
            alertDialog.show();
        }

    }

    /**
     * Check if the device is connected to any network.
     * If not, display a dialog message
     */
    public void checkNetworkAvailability(){
        if(!MQTTFactory.isNetworkAvailable(SecureChat.this)){
            AlertDialog alertDialog = new AlertDialog.Builder(SecureChat.this).create();
            alertDialog.setTitle("Warning!");
            alertDialog.setMessage("Please make sure your device has internet connectivity");
            alertDialog.setCancelable(false);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkNetworkAvailability();
                        }
                    });
            alertDialog.show();
        }
    }

    /**
     * Handling back button press in the device activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityContexts.setCurrentActivityContext(ActivityContexts.getMainActivityContext());
    }

}
