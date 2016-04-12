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
package com.swissbit.homeautomation.db;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.swissbit.homeautomation.model.Device;
import com.swissbit.homeautomation.model.RaspberryPi;
import com.swissbit.homeautomation.utils.ActivityContexts;
import com.swissbit.homeautomation.utils.DBConstants;

import java.util.Random;

/**
 * Database class of the application
 */
public class ApplicationDb {

    /**
     * Helper class object
     */
    DevicesInfoDbInner helper;

    /**
     * SQLite object
     */
    SQLiteDatabase db;

    /**
     * Constructor
     */
    public ApplicationDb(Context context){
        helper = new DevicesInfoDbInner(context);
        db = helper.getWritableDatabase();
    }

    /**
     * Insert a RaspberryPi
     */
    public long insertRaspberry(String rid, String secureElementId, String name, String desc){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.RASPBERRYID, rid);
        contentValues.put(DBConstants.SECURE_ELEMENT_ID,secureElementId);
        contentValues.put(DBConstants.RASPBERRYNAME, name);
        contentValues.put(DBConstants.RASPBERRYDESC, desc);
        long id = db.insert(DBConstants.TABLE_NAME_RASPBERRYINFO, null, contentValues);
        return id;
    }

    /**
     * Get a Raspberry by its ID
     */
    public String getRaspberryId(){
        String[] columns= {DBConstants.RASPBERRYID};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_RASPBERRYINFO, columns, null,
                null, null, null, null);

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndex(DBConstants.RASPBERRYID));
            return id;
        }
        return null;
    }

    /**
     * Get for a duplicate Raspberry by its ID
     */
    public boolean checkRaspberryId(String id){
        String[] columns= {DBConstants.RASPBERRYID};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_RASPBERRYINFO, columns, DBConstants.RASPBERRYID + " = '" + id + "'",
                null, null, null, null);

        if(cursor.getCount() != 0)
            return false;
        else
            return true;
    }

    /**
     * Get a Raspberry details
     */
    public RaspberryPi getRaspberry(){
        String[] columns= {DBConstants.RASPBERRYID,DBConstants.SECURE_ELEMENT_ID,DBConstants.RASPBERRYNAME,DBConstants.RASPBERRYDESC};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_RASPBERRYINFO, columns, null,
                null, null, null, null);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndex(DBConstants.RASPBERRYID));
            String secureElementId = cursor.getString(cursor.getColumnIndex(DBConstants.SECURE_ELEMENT_ID));
            String name = cursor.getString(cursor.getColumnIndex(DBConstants.RASPBERRYNAME));
            String desc = cursor.getString(cursor.getColumnIndex(DBConstants.RASPBERRYDESC));
            RaspberryPi raspberryPi = RaspberryPi.createRaspberryPi(id, secureElementId, name, desc, false);

            return raspberryPi;
        }
            return null;
    }

    /**
     * Check if the secret code has been asked
     */
    public int checkSecretCodeDialogShow(){
        String[] columns = {DBConstants.DIALOGSHOW};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_CREDENTIALS,columns,null,null,null,null,null);
        if(cursor.getCount()!=0) {
            cursor.moveToFirst();
            Log.d("Dialog","1");
            return cursor.getInt(cursor.getColumnIndex(DBConstants.DIALOGSHOW));
        }
        Log.d("Dialog","0");
        return 0;
    }

    /**
     * Set credentials to connect to the broker
     */
    public void setCredentials(String code, String username, String password){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.CODE,code);
        contentValues.put(DBConstants.USERNAME,username);
        contentValues.put(DBConstants.PASSWORD,password);
        contentValues.put(DBConstants.CLIENTID,Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
        contentValues.put(DBConstants.DIALOGSHOW,1);

        db.update(DBConstants.TABLE_NAME_CREDENTIALS, contentValues, null, null);
        Log.d("After Set", "SetCred");
    }

    /**
     * Get the credentials to connect the broker
     */
    public String[] getCredentials() {
        String[] columns = {DBConstants.USERNAME, DBConstants.PASSWORD, DBConstants.CLIENTID};
        String[] credentials = new String[3];
        Cursor cursor = db.query(DBConstants.TABLE_NAME_CREDENTIALS, columns, null,
                null, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            credentials[0] = cursor.getString(cursor.getColumnIndex(DBConstants.USERNAME));
            credentials[1] = cursor.getString(cursor.getColumnIndex(DBConstants.PASSWORD));
            credentials[2] = cursor.getString(cursor.getColumnIndex(DBConstants.CLIENTID));
        }
        return credentials;
    }

    /**
     * Insert a device
     */
    public long insertDevice(int deviceId,String raspberryId,String raspberryName, String raspberryDesc, String deviceStatus){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.DEVICE_NODE_ID, deviceId);
        contentValues.put(DBConstants.RASPBERRYID, raspberryId);
        contentValues.put(DBConstants.DEVICE_NAME, raspberryName);
        contentValues.put(DBConstants.DEVICE_DESCRIPTION, raspberryDesc);
        contentValues.put(DBConstants.DEVICE_STATUS,deviceStatus);
        long id = db.insert(DBConstants.TABLE_NAME_DEVICES, null, contentValues);
        return id;
    }

    /**
     * Check a device by its ID
     */
    public boolean checkDeviceById(int id){
        String[] columns = {DBConstants.DEVICE_NODE_ID};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_DEVICES, columns, DBConstants.DEVICE_NODE_ID + " = '" + id + "'",
                null, null, null, null);
        if(cursor.getCount() != 0)
            return true;
        return false;
    }

    /**
     * Get a Device by its ID
     */
    public Device getDevice(){
        String[] columns= {DBConstants.DEVICE_NODE_ID,DBConstants.RASPBERRYID,DBConstants.DEVICE_NAME,DBConstants.DEVICE_DESCRIPTION,
                            DBConstants.DEVICE_STATUS};
        Cursor cursor = db.query(DBConstants.TABLE_NAME_DEVICES, columns, null,
                null, null, null, null);
        if(cursor.getCount() != 0) {
            cursor.moveToFirst();
            int deviceId = cursor.getInt(cursor.getColumnIndex(DBConstants.DEVICE_NODE_ID));
            String raspberryId = cursor.getString(cursor.getColumnIndex(DBConstants.RASPBERRYID));
            String deviceName = cursor.getString(cursor.getColumnIndex(DBConstants.DEVICE_NAME));
            String deviceDesc = cursor.getString(cursor.getColumnIndex(DBConstants.DEVICE_DESCRIPTION));
            String deviceStatus = cursor.getString(cursor.getColumnIndex(DBConstants.DEVICE_STATUS));
            Device device = Device.createDevice(deviceId,raspberryId,deviceName,deviceDesc,deviceStatus);

            return device;
        }
        return null;
    }

    /**
     * Update the status of the device
     */
    public void updateDeviceStatus(String status,int nodeId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstants.DEVICE_STATUS,status);
        db.update(DBConstants.TABLE_NAME_DEVICES, contentValues, null, null);
    }

    /**
     * Reset the application data
     */
    public void resetData(){
        try {
            DevicesInfoDbInner dbRecreate = new DevicesInfoDbInner(ActivityContexts.getMainActivityContext());
            dbRecreate.onUpgrade(db,1,2);

        } catch(SQLException e){
            Log.e("ResetDb", "" +e);
        }
    }

    /**
     * Inner helper class
     */
    static class DevicesInfoDbInner extends SQLiteOpenHelper{
        private Context context;
        public DevicesInfoDbInner(Context context) {
            super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
            Log.d("1","Constructor Called");
        }

        /**
         * Creation of the database
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DBConstants.CREATE_TABLE_RASPBERRYINFO);
                db.execSQL(DBConstants.CREATE_TABLE_CREDENTIALS);
                db.execSQL(DBConstants.CREATE_TABLE_DEVICES);
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBConstants.DIALOGSHOW, 0);
                db.insert(DBConstants.TABLE_NAME_CREDENTIALS, null, contentValues);

            } catch(SQLException e){
                Log.e("CreateDb", "" +e);
            }
        }

        /**
         * Update of the database
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DBConstants.DROP_TABLE_RASPBERRYINFO);
                db.execSQL(DBConstants.DROP_TABLE_CREDENTIALS);
                db.execSQL(DBConstants.DROP_TABLE_DEVICES);
                onCreate(db);
            } catch(SQLException e){
                Log.e("UpgradeDb", "" +e);
            }
        }
    }

}
