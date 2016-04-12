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

package com.swissbit.homeautomation.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.common.collect.Lists;
import com.swissbit.homeautomation.asyncTask.AuthenticationAsync;
import com.android.swissbit.homeautomation.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.swissbit.homeautomation.asyncTask.HeartBeatAsync;
import com.swissbit.homeautomation.asyncTask.LWTAsync;
import com.swissbit.homeautomation.asyncTask.PermissionRevocationAsync;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.model.RaspberryPi;
import com.swissbit.homeautomation.ui.adapter.RPiAdapter;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.utils.EncryptionFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.homeautomation.ui.dialog.SecureCodeDialog;
import com.tum.ssdapi.CardAPI;
import java.util.List;

/**
 * This is an android application for SwissBit's Home Automation Solution.
 *
 * This is the Main activity of the application.
 * This activity is responsible for authenticating and addition of RaspberryPi
 */

public class MainActivity extends ActionBarActivity {

    /**
     * The list view for the RaspberryPi
     */
    private ListView listView;

    /**
     * The model object of RaspberryPi
     */
    private RaspberryPi raspberryPi;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     * The object to access the secure element for the SD card
     */
    private CardAPI secureElementAccess;

    /**
     * The secure element ID of the SD card
     */
    private String secureElementId;

    /**
     * The AsyncTask responsible for handling permission revocation
     */
    private PermissionRevocationAsync permissionRevocationAsync;

    /**
     * The AsyncTask responsible for handling heartbeat
     */
    private HeartBeatAsync heartBeatAsync;

    /**
     * The AsyncTask responsible for reporting when the RaspberryPi incurs sudden crash
     */
    private LWTAsync lwtAsync;

    /**
     * OnCreate method of the activity.
     * Checks if the action has been previously revoked.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Save the context of MainActivity for later usage in other classes
        ActivityContexts.setMainActivityContext(this);

        applicationDb = DBFactory.getDevicesInfoDbAdapter(this);
        listView = (ListView) findViewById(R.id.listRaspberryPi);
        listView.setEmptyView(findViewById(R.id.empty_list_item));

        secureElementAccess = new CardAPI(getApplicationContext());

        permissionRevocationAsync = new PermissionRevocationAsync(this);

        heartBeatAsync = new HeartBeatAsync();

        lwtAsync = new LWTAsync();

        //To check for network availability
        checkNetworkAvailability();

        //To check if access has been revoked
        checkAccessRevoked();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Menu Items for RaspberryPi addition and reset of application data
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //To add a RaspberryPi
        if (id == R.id.register_raspberry) {
            if(DBFactory.getRaspberry() != null){
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Information");
                alertDialog.setMessage("RaspberryPi already added");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Caution!");
                alertDialog.setMessage("Please make sure your RaspberryPi was turned on atleast 1 minute ago");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                //Calling the QR scanner app via intent
                                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                integrator.initiateScan();
                            }
                        });
                alertDialog.show();
            }
        }

        //To reset application data
        if (id == R.id.reset_data) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Caution!");
            alertDialog.setMessage("Are you sure you want to reset all data? Application will be restarted in this case ");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            applicationDb.resetData();
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alertDialog.show();

        }

        if (id == R.id.secure_chat) {
            Intent intent = new Intent(MainActivity.this, ChatInitiateActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Used to get the secure code from the user
     */
    public void getSecureCode() {

        if (applicationDb.checkSecretCodeDialogShow() == 0) {
            SecureCodeDialog secureCodeDialog = new SecureCodeDialog();
            secureCodeDialog.getSecureCode(this);
        }
    }

    /**
     * Gets the QR code from the QR scanner.
     * Checks and authenticates the QR code (Secure element Id)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            String qrCode = scanResult.getContents();
            String qrCodeArray[] = qrCode.split("#");
            secureElementId = qrCodeArray[0];
            String rid = qrCodeArray[1];
            Log.d("Rid", rid);
            Log.d("Sid", secureElementId);

            MQTTFactory.setRaspberryId(rid);
            MQTTFactory.setSecureElementId(secureElementId);

            Toast.makeText(getApplicationContext(), rid, Toast.LENGTH_SHORT).show();

            Log.d("Secureelement", "" + secureElementAccess.isCardPresent());

            if (secureElementAccess.isCardPresent()) {
                String encryptedString = secureElementAccess.encryptMsgWithID(secureElementId, rid);
                EncryptionFactory.setEncryptedString(encryptedString);
                Log.d("Encrypted Data", EncryptionFactory.getEncryptedString());
                AuthenticationAsync authenticationAsync = new AuthenticationAsync(this, MQTTFactory.getRaspberryPiById());
                authenticationAsync.execute();
            } else {
                Toast.makeText(getApplicationContext(), "Secure SD card not present", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     *Check for duplicate RaspberryPi
     */
    public void checkRaspberryId(String rid) {

        if (DBFactory.checkRaspberryPiInDB(rid, this)) {
            DBFactory.addRaspberryPi(rid, secureElementId);
            addToListView();
        } else
            Toast.makeText(getApplicationContext(), "Duplicate Server", Toast.LENGTH_LONG).show();
    }

    /**
     * Display the RaspberryPi in the list view
     */
    public void addToListView() {

        //The list of RaspberryPi's
        List<RaspberryPi> listOfRPi;

        //The custom adapter for RaspberryPi list view
        RPiAdapter adapter;

        raspberryPi = DBFactory.getRaspberry();

        if (raspberryPi != null) {
            MQTTFactory.setRaspberryId(raspberryPi.getId());
            listOfRPi = Lists.newArrayList(raspberryPi);
            adapter = new RPiAdapter(getApplicationContext(), listOfRPi);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //Check if the RaspberryPi has received heartbeats. If not, do not allow to launch device activity
                    View rootView = ((Activity) ActivityContexts.getMainActivityContext()).getWindow().getDecorView().findViewById(android.R.id.content);
                    ImageView imgStatus = (ImageView) rootView.findViewById(R.id.imgStatus);
                    if((int)imgStatus.getTag() == R.drawable.btnon){
                        RaspberryPi clickedItem = (RaspberryPi) listView.getItemAtPosition(position);

                        //Start the device activity
                        Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("RaspberryId", clickedItem.getId());
                        extras.putString("SecureElementId", raspberryPi.getSecureElementId());
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "RaspberryPi currently offline. Wait till connectivity is established", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Start all the async tasks

            heartBeatAsync.execute();

            lwtAsync.execute();

            permissionRevocationAsync.execute();
        }

    }

    /**
     * Used to check if the access has been previously revoked when the application starts.
     * Post this check ask for the secure code only for the first time.
     * Then, adds the RaspberryPi to the listview
     */
    public void checkAccessRevoked(){

        Log.d("SDEnabled", secureElementAccess.getEnabled().toString());
        if("029000".equals(secureElementAccess.getEnabled().toString())) {
            Log.d("Inside Alert","Alert");
            Context context = MainActivity.this;
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
        else {
            //Get the secure Code
            getSecureCode();

            //Display RaspberryPi in the list view
            addToListView();
        }
    }

    /**
     * Check if the device is connected to any network.
     * If not, display a dialog message
     */
    public void checkNetworkAvailability(){
        if(!MQTTFactory.isNetworkAvailable(MainActivity.this)){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
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
}
