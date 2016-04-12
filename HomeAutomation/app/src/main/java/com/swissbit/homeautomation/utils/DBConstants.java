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
package com.swissbit.homeautomation.utils;

/**
 * Database constants
 */
public interface DBConstants {

    /**
     * Database Name
     */
    public static final String DATABASE_NAME="ApplicationDb";

    /**
     * Database Version
     */
    public static final int DATABASE_VERSION = 36;

    /**
     * Column Names for the table RaspberryInfo
     * TABLE_NAME_RASPBERRYINFO - Name of the table
     * UID - Auto generated id
     * RASPBERRYID - Id of the RaspberryPi
     * SECURE_ELEMENT_ID - Id of the secure element (SD card)
     * RASPBERRYNAME - Name of the RaspberryPi
     * RASPBERRYDESC - Description of the RaspberryPi
     */
    public static final String TABLE_NAME_RASPBERRYINFO="RaspberryInfo";
    public static final String UID = "_id";
    public static final String RASPBERRYID = "RaspberryId";
    public static final String SECURE_ELEMENT_ID = "SecureElementId";
    public static final String RASPBERRYNAME = "RaspberryName";
    public static final String RASPBERRYDESC = "RaspberryDesc";

    /**
     * Column Names for the table Credentials
     * This holds the username and password to connect to the broker.
     * TABLE_NAME_CREDENTIALS - Name of the table
     * CODE - Secret code
     * USERNAME - Username of the broker
     * PASSWORD - Password of the broker
     * DIALOGSHOW - To check if the secure code has been asked before
     * CLIENTID - Id of the mobile device
     */
    public static final String TABLE_NAME_CREDENTIALS="Credentials";
    public static final String CODE = "Code";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String DIALOGSHOW = "DialogShow";
    public static final String CLIENTID = "ClientId";

    /**
     * Column Names for the table Devices
     * TABLE_NAME_DEVICES - Name of the table
     * DEVICE_UID - Auto generated id
     * DEVICE_NODE_ID - Id of the device assigned by the RaspberryPI
     * DEVICE_NAME - Name of the device
     * DEVICE_DESCRIPTION - Description of the device
     * DEVICE_STATUS - Last known status of the device
     */
    public static final String TABLE_NAME_DEVICES="Devices";
    public static final String DEVICE_UID = "_id";
    public static final String DEVICE_NODE_ID = "DeviceId";
    public static final String DEVICE_NAME= "DeviceName";
    public static final String DEVICE_DESCRIPTION = "DeviceDescription";
    public static final String DEVICE_STATUS= "DeviceStatus";

    /**
     * Create command for the table RaspberryInfo
     */
    public static final String CREATE_TABLE_RASPBERRYINFO = "CREATE TABLE " + TABLE_NAME_RASPBERRYINFO
            + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + RASPBERRYID
            + " VARCHAR(50), " + SECURE_ELEMENT_ID
            + " VARCHAR(50), " + RASPBERRYNAME
            + " VARCHAR(25), " + RASPBERRYDESC
            + " VARCHAR(50));";

    /**
     * Create command for the table Credentials
     */
    public static final String CREATE_TABLE_CREDENTIALS = "CREATE TABLE " + TABLE_NAME_CREDENTIALS
            + " (" + CODE + " VARCHAR(10) DEFAULT NULL, " + USERNAME
            + " VARCHAR(15), " + PASSWORD
            + " VARCHAR(15), " + CLIENTID
            + " VARCHAR(15), " + DIALOGSHOW
            + " INTEGER DEFAULT 0);";

    /**
     * Create command for the table Devices
     */
    public static final String CREATE_TABLE_DEVICES = "CREATE TABLE " + TABLE_NAME_DEVICES
            + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DEVICE_NODE_ID
            + " INTEGER, " + RASPBERRYID
            + " VARCHAR(50), " + DEVICE_NAME
            + " VARCHAR(25), " + DEVICE_DESCRIPTION
            + " VARCHAR(50), " + DEVICE_STATUS
            + " VARCHAR(15), "
            + " FOREIGN KEY(" + RASPBERRYID + ") REFERENCES " + TABLE_NAME_RASPBERRYINFO + "(" + RASPBERRYID + ") ON DELETE CASCADE);";

    /**
     * Drop command for the table RaspberryInfo
     */
    public static final String DROP_TABLE_RASPBERRYINFO = "DROP TABLE IF EXISTS " + TABLE_NAME_RASPBERRYINFO;
    /**
     * Drop command for the table Credentials
     */
    public static final String DROP_TABLE_CREDENTIALS = "DROP TABLE IF EXISTS " + TABLE_NAME_CREDENTIALS;

    /**
     * Drop command for the table Devices
     */
    public static final String DROP_TABLE_DEVICES = "DROP TABLE IF EXISTS " + TABLE_NAME_DEVICES;

}
