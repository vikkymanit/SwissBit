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

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioButton;

import com.android.swissbit.homeautomation.R;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.WSConstants;
import com.tum.ssdapi.CardAPI;

/**
 * This activity initiates the secure chat.
 */

public class ChatInitiateActivity extends ActionBarActivity {

    /**
     * The Id of the conversation initiator
     */
    private String recId;

    /**
     * The object to access the secure element for the SD card
     */
    private CardAPI secureElementAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Save the context of DeviceActivity for later usage in other classes
        ActivityContexts.setChatActivityContext(this);

        Button initiate = (Button) findViewById(R.id.btnJoin);
        secureElementAccess = new CardAPI(getApplicationContext());

        initiate.setOnClickListener
                (new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         //If no Radio Button Selected Reject going ahead
                         EditText displayName = (EditText) findViewById(R.id.name);

                         RadioGroup g = (RadioGroup) findViewById(R.id.secure_ids);
                         int idSelected =  g.getCheckedRadioButtonId();
                         if(idSelected == -1){
                            Toast.makeText(ChatInitiateActivity.this, "Please select one of Ids.", Toast.LENGTH_SHORT).show();
                         }
                         else if(displayName.getText().toString().equals("")){
                             Toast.makeText(ChatInitiateActivity.this, "Display Name cannot be blank.", Toast.LENGTH_SHORT).show();
                         }
                         else if(!secureElementAccess.isCardPresent()){
                             Toast.makeText(getApplicationContext(), "Secure SD card not present", Toast.LENGTH_SHORT).show();
                         }
                         else {
                             // Redirect to chat activity on success.
                             Intent intent = new Intent(ChatInitiateActivity.this, SecureChat.class);
                             EditText name = (EditText) findViewById(R.id.name);
                             intent.putExtra("name", name.getText().toString());
                             intent.putExtra("recId", recId);
                             startActivity(intent);
                         }
                     }
                 }

                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_chat, menu);
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
     * Handling back button press in the device activity
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityContexts.setCurrentActivityContext(ActivityContexts.getMainActivityContext());
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_id1:
                if (checked)
                    recId = WSConstants.secureIds[0];
                    break;
            case R.id.radio_id2:
                if (checked)
                    recId = WSConstants.secureIds[1];
                    break;
        }
    }
}
