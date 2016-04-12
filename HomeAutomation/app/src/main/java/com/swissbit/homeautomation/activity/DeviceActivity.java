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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;
import com.android.swissbit.homeautomation.R;
import com.google.common.collect.Lists;
import com.swissbit.homeautomation.asyncTask.DeviceStatusRefreshAsync;
import com.swissbit.homeautomation.asyncTask.RetrieveDeviceListAsync;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.model.Device;
import com.swissbit.homeautomation.ui.adapter.DeviceAdapter;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;

import java.util.List;

/**
 * This activity handles all the functionality related to the devices.
 */
public class DeviceActivity extends ActionBarActivity {

    /**
     *List view for the devices
     */
    private ListView deviceListView;

    /**
     * RaspberryPi Id
     */
    private String raspberryId;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     * OnCreate method of the activity.
     * Gets the RaspberryPi id from MainActivity and makes a call to retrieve the devices attached to it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);

        //Save the context of DeviceActivity for later usage in other classes
        ActivityContexts.setDeviceActivityContext(this);
        applicationDb = DBFactory.getDevicesInfoDbAdapter(this);

        deviceListView = (ListView) findViewById(R.id.listDevice);

        Bundle extras;
        extras = getIntent().getExtras();
        raspberryId = extras.getString("RaspberryId");

        MQTTFactory.setSecureElementId(extras.getString("SecureElementId"));

        //To get the list of devices attached to the RaspberryPi
        getDeviceList();


    }

    /**
     * To get the list of devices attached to the RaspberryPi.
     * The device list retrieval happens only once.
     * If the list is already retrieved, then the status of device is retrieved.
     */
    public void getDeviceList(){
        if(applicationDb.getDevice() == null){
            RetrieveDeviceListAsync retrieveDeviceListAsync = new RetrieveDeviceListAsync(raspberryId);
            retrieveDeviceListAsync.execute();
        }
        else{
            DeviceStatusRefreshAsync deviceStatusRefreshAsync = new DeviceStatusRefreshAsync();
            deviceStatusRefreshAsync.execute();
        }

    }

    /**
     * Display the device in the list view
     */
    public void addToListView() {

        DeviceAdapter adapter;
        Device device;
        List<Device> listOfDevice;

        device = applicationDb.getDevice();
        listOfDevice = Lists.newArrayList(device);

        adapter = new DeviceAdapter(getApplicationContext(), listOfDevice);

        deviceListView.setAdapter(adapter);


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



