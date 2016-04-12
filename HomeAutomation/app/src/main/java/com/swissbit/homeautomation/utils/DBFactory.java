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

import android.content.Context;
import android.util.Log;

import com.swissbit.homeautomation.db.ApplicationDb;
import com.swissbit.homeautomation.model.RaspberryPi;

/**
 * Database factory to get the database instances
 */
public final class DBFactory {

    /**
     * The database object of the application
     */
    private static ApplicationDb applicationDb;

    /**
     * Returns the database object for the application
     */
    public static ApplicationDb getDevicesInfoDbAdapter(Context context) {
        if (applicationDb == null)
            applicationDb = new ApplicationDb(context);

        return applicationDb;
    }

    /**
     * Adds a RaspberryPi in the database
     */
    public static void addRaspberryPi(String rid, String secureElementId){
        long id = applicationDb.insertRaspberry(rid, secureElementId, "Raspberry", rid);
        if(id<0){
            Log.d("Insert", "Failed to insert");
        }
        else {
            Log.d("Insert", "Successful insert");
        }

    }

    /**
     * Check if a RaspberryPi exists in the database to prevent duplicate RaspberryPi addition
     */
    public static boolean checkRaspberryPiInDB(String rid, Context context) {
        return getDevicesInfoDbAdapter(context).checkRaspberryId(rid);
    }

    /**
     * Return a RaspberryPi from the database
     */
    public static RaspberryPi getRaspberry(){
        return applicationDb.getRaspberry();
    }
}
