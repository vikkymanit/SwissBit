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
package com.swissbit.homeautomation.ws;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.ui.dialog.SecureCodeDialog;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.utils.WSConstants;
import com.swissbit.mqtt.client.IKuraMQTTClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *WebService to verify the secret code and upon successful verification get the username and password of
 * the broker. Future communication takes place with the broker.
 */
public class VerifySecretCode {

    /**
     * Username to connect to the broker
     */
    private String username;

    /**
     * Password to connect to the broker
     */
    private String password;

    /**
     * The secret code
     */
    private String secretCode;

    /**
     *Main Activity context
     */
    private Context mainContext;

    /**
     * Mqtt client
     */
    private IKuraMQTTClient client;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     * Constructor
     */
    public VerifySecretCode(Context context,ApplicationDb applicationDb, String secretCode) {
        this.applicationDb = applicationDb;
        this.secretCode = secretCode;
        this.mainContext = context;
        client = null;
    }

    /**
     * Execute the webservice
     */
    public void executeCredentialWS() {

        if(secretCode.length() == 0){
            SecureCodeDialog secureCodeDialog = new SecureCodeDialog();
            secureCodeDialog.getSecureCode(mainContext);
        }

        //Call the webservice
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(WSConstants.CREDENTIAL_WS + secretCode, new JsonHttpResponseHandler() {

            //If the response is a JSON object. Retrieve the username and password to connect to broker.
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("DEBUG SWISS", "JSONObject");
                    username = (String) response.get("username");
                    password = (String) response.get("password");
                    Log.d("WSCred", username);
                    Log.d("WSCred", password);
                    if (username != null && password != null)
                        applicationDb.setCredentials(secretCode, username, password);
                    client = MQTTFactory.getClient();
                    boolean status = client.connect();
                    Log.d("After Dialog", "" + status);
                    showValidatedDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //If the response is a JSON Array. Retrieve the username and password to connect to broker.
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG SWISS", "JSONArray");
                JSONObject usernameTmp;
                JSONObject passwordTmp;
                try {
                    usernameTmp = (JSONObject) response.get(0);
                    passwordTmp = (JSONObject) response.get(1);
                    username = usernameTmp.getString("username");
                    password = passwordTmp.getString("password");
                    if (username != null && password != null)
                        applicationDb.setCredentials(secretCode, username, password);
                    Log.d("WSCred", username);
                    Log.d("WSCred", password);
                    client = MQTTFactory.getClient();
                    boolean status = client.connect();
                    Log.d("After Dialog", "" + status);
                    showValidatedDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //If the webservice returns failure message
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG SWISS", "INSIDE 4" + throwable.getCause());
                try {
                    Toast.makeText(mainContext, "Invalid Code", Toast.LENGTH_LONG).show();
                    SecureCodeDialog secureCodeDialog = new SecureCodeDialog();
                    secureCodeDialog.getSecureCode(mainContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //If the webservice returns failure message
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("DEBUG SWISS", "INSIDE 5");
                try {
                    Toast.makeText(mainContext, "Invalid Code", Toast.LENGTH_LONG).show();
                    SecureCodeDialog secureCodeDialog = new SecureCodeDialog();
                    secureCodeDialog.getSecureCode(mainContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Show the dialog when secret code is validated
     */
    public void showValidatedDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(mainContext).create();
        alertDialog.setTitle("Information");
        alertDialog.setMessage("Secure Code Validated");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
