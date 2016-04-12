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

/**
 * Saves the activity contexts of all the activities
 */
public class ActivityContexts {

    /**
     * MainActivity context
     */
    private static Context mainActivityContext;

    /**
     * DeviceActivity context
     */
    private static Context deviceActivityContext;

    public static Context getChatActivityContext() {
        return chatActivityContext;
    }

    public static void setChatActivityContext(Context chatActivityContext) {
        ActivityContexts.chatActivityContext = chatActivityContext;
    }

    /**
     * ChatActivity context
     */
    private static Context chatActivityContext;


    /**
     * Current Activity context
     */
    private static Context currentActivityContext;

    public static Context getCurrentActivityContext() {
        return currentActivityContext;
    }

    public static void setCurrentActivityContext(Context currentActivityContext) {
        ActivityContexts.currentActivityContext = currentActivityContext;
    }

    public static Context getMainActivityContext() {
        return mainActivityContext;
    }

    public static void setMainActivityContext(Context mainActivityContext) {
        ActivityContexts.mainActivityContext = mainActivityContext;
        setCurrentActivityContext(mainActivityContext);
    }

    public static Context getDeviceActivityContext() {
        return deviceActivityContext;
    }

    public static void setDeviceActivityContext(Context deviceActivityContext) {
        ActivityContexts.deviceActivityContext = deviceActivityContext;
        setCurrentActivityContext(deviceActivityContext);
    }

}
