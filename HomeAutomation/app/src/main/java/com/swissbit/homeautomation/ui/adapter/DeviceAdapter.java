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
package com.swissbit.homeautomation.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.asyncTask.DeviceCmdAsync;
import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.model.Device;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.utils.MQTTFactory;
import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.message.KuraPayload;

import java.util.List;

/**
 * Custom Adapter to display device in the list view
 */

public class DeviceAdapter extends ArrayAdapter<Device> {

    /**
     * Info of list of devices
     */
    private List<Device> deviceInfo;

    /**
     * Device object
     */
    private Device device;

    /**
     * Constructor
     */
    public DeviceAdapter(Context context, List<Device> deviceInfo) {
        super(context, R.layout.row_device_details, deviceInfo);
        this.deviceInfo = deviceInfo;
        device = deviceInfo.get(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.row_device_details, parent, false);

        ImageView imageDevice = (ImageView) customView.findViewById(R.id.imgDevice);
        Switch socketSwitch = (Switch) customView.findViewById(R.id.socketSwitch);

        //Set the status of the device based on the last known status
        if (device.getStatus().equals("true")){
            socketSwitch.setChecked(true);
            imageDevice.setImageResource(R.drawable.socketswitchon);
        }
        else{
            socketSwitch.setChecked(false);
            imageDevice.setImageResource(R.drawable.socketswitchoff);
        }

        //Attach change listner to handle on/off commands
        socketSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Inside listner", "1");
                if (isChecked) {
                    Log.d("Task Started..", "1");
                    DeviceCmdAsync deviceCmdAsync = new DeviceCmdAsync("on", device.getDeviceNodeId(),device.getRaspberryId());
                    deviceCmdAsync.execute();
                    Log.d("Task done..", "1");
                } else {
                    Log.d("Task Started..", "1");
                    DeviceCmdAsync deviceCmdAsync = new DeviceCmdAsync("off", device.getDeviceNodeId(),device.getRaspberryId());
                    deviceCmdAsync.execute();
                    Log.d("Task done..", "1");
                }

            }
        });

        return customView;
    }


}
