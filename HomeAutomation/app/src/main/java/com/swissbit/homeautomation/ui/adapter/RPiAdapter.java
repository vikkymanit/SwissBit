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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.model.RaspberryPi;

import java.util.List;

/**
 * Custom Adapter to display RaspberryPi in the list view
 */

public class RPiAdapter extends ArrayAdapter<RaspberryPi> {

    /**
     * Info of list of RaspberryPi
     */
    private List<RaspberryPi> raspberryInfo;

    /**
     * RaspberryPi object
     */
    private RaspberryPi raspberryPi;

    /**
     * Constructor
     */
    public RPiAdapter(Context context, List<RaspberryPi> raspberryInfo) {
        super(context, R.layout.row_raspberry_details, raspberryInfo);
        this.raspberryInfo = raspberryInfo;
        raspberryPi = raspberryInfo.get(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customView = layoutInflater.inflate(R.layout.row_raspberry_details_full, parent, false);
        ViewGroup viewGroup = parent;

//        TextView raspberryName = (TextView) customView.findViewById(R.id.txtRaspberryName);
        TextView raspberryDescription = (TextView) customView.findViewById(R.id.txtRaspberryDescription);
        TextView raspberryId = (TextView) customView.findViewById(R.id.txtRaspberryId);
        TextView raspberryStatus = (TextView) customView.findViewById(R.id.txtRaspberryStatus);
        ImageView imageRaspberry = (ImageView) customView.findViewById(R.id.imgRaspberry);
        ImageView imageStatus = (ImageView) customView.findViewById(R.id.imgStatus);

//        raspberryName.setText(raspberryPi.getName());
        raspberryId.setText(raspberryPi.getId());

        //Initial status of the RaspberryPi
        if (raspberryPi.getStatus()) {
            imageStatus.setImageResource(R.drawable.btnon);
            imageStatus.setTag(R.drawable.btnon);
        }
        else {
            imageStatus.setImageResource(R.drawable.btnoff);
            imageStatus.setTag(R.drawable.btnoff);
        }
        return customView;
    }


}
