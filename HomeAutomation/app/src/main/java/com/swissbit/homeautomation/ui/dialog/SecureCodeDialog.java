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
package com.swissbit.homeautomation.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.utils.DBFactory;
import com.swissbit.homeautomation.ws.VerifySecretCode;

/**
 * Dialog for getting the secure code. 
 * This is done only once. Happens when the first time the application is launched after installation
 */
public class SecureCodeDialog {

    /**
     *Main Activity context
     */
    private Context mainContext;

    /**
     *Web service object to verify the secure code
     */
    private VerifySecretCode verifySecretCode;

    /**
     * The database object of the application
     */
    private ApplicationDb applicationDb;

    /**
     * The secret code
     */
    private String secretCode;

    /**
     * Get the secret code
     */
    public void getSecureCode(Context context){
        this.mainContext = context;
        applicationDb = DBFactory.getDevicesInfoDbAdapter(context);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        final EditText txtCode = new EditText(context);
        txtCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        dialogBuilder.setTitle("Enter the Secure Code");
        dialogBuilder.setMessage("Enter the Secure Code");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(txtCode);
        dialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                secretCode = txtCode.getText().toString().trim();
                Log.d("After Dialog", "Trim"+secretCode);

                //Execute the web service to verify the secure code
                verifySecretCode = new VerifySecretCode(mainContext, applicationDb, secretCode);
                verifySecretCode.executeCredentialWS();
                Log.d("After Set","ExecWS");

            }
        });

        AlertDialog dialogSecretCode = dialogBuilder.create();
        dialogSecretCode.show();
    }
}
