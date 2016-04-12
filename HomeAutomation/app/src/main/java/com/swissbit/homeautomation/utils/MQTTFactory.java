/**
 * ****************************************************************************
 * Copyright (C) 2015 - Manit Kumar <vikky_manit@yahoo.co.in>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.swissbit.homeautomation.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.swissbit.mqtt.client.IKuraMQTTClient;
import com.swissbit.mqtt.client.KuraMQTTClient;
import com.swissbit.mqtt.client.message.KuraPayload;
import com.tum.ssdapi.CardAPI;

import java.util.Random;

/**
 * Factory class for Mqtt connections, topic builder for all subscription and publishing
 */
public final class MQTTFactory {

    /**
     * ClientId of the mobile device
     */
    private static String clientId;

    /**
     * Id of the Raspberry
     */
    private static String raspberryId;

    /**
     * Id of the Secure element (SD card)
     */
    private static String secureElementId;

    /**
     * Mqtt connection interface
     */
    private static IKuraMQTTClient iKuraMQTTClient;

    /**
     * The object to access the secure element for the SD card
     */
    private static CardAPI secureElementAccess;

    /**
     * Get the client Id of the mobile device
     */
    public static String getClientId() {
        if (clientId == null)
            clientId = DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getMainActivityContext()).getCredentials()[2];
        Log.d("Kura MQTT",clientId);
        return clientId;
    }

    /**
     * Get the Mqtt client to connect to the broker
     */
    public static synchronized IKuraMQTTClient getClient() {

        if (iKuraMQTTClient == null){
            iKuraMQTTClient = new KuraMQTTClient.Builder()
                    .setHost("m20.cloudmqtt.com").setPort("13273")
                    .setClientId(getClientId()).setUsername(getUsername())
                    .setPassword(getPassword()).build();
        }
        return iKuraMQTTClient;
    }

    /**
     * Get the username to connect to the broker from database
     */
    public static String getUsername() {

        return DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getMainActivityContext()).getCredentials()[0];
    }

    /**
     * Get the password to connect to the broker from database
     */
    public static String getPassword() {

        return DBFactory.getDevicesInfoDbAdapter(ActivityContexts.getMainActivityContext()).getCredentials()[1];
    }

    /**
     * Set the RaspberryPi Id
     */
    public static void setRaspberryId(String raspberryId) {
        MQTTFactory.raspberryId = raspberryId;
    }

    /**
     * Get the RaspberryPi Id
     */
    public static String getRaspberryPiById() {
        return raspberryId;
    }

    /**
     * Get the Id of the secure element (SD card)
     */
    public static String getSecureElementId() {
        return secureElementId;
    }

    /**
     * Get the Id of the secure element (SD card)
     */
    public static String getMobileClientSecureElementId(){
        if(secureElementAccess == null)
            secureElementAccess = new CardAPI(ActivityContexts.getMainActivityContext());
        return secureElementAccess.getMyId();
    }

    /**
     * Set the Id of the secure element.
     * Just for use in the application and Not the actual ID in the SD card.
     */
    public static void setSecureElementId(String secureElementId) {
        MQTTFactory.secureElementId = secureElementId;
    }

    /**
     * Gets the various topics to subscribe
     */
    public static String[] getTopicToSubscribe(String id) {

        final String requestId = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        switch (id) {
            case TopicsConstants.LWT:
                return new String[]{getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.LWT};

            case TopicsConstants.HEARTBEAT:
                return new String[]{getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.HEARTBEAT + "mqtt/heartbeat"};

            case TopicsConstants.RASPBERRY_AUTH_SUB:
                return new String[]{getMQTTTopicPrefix(TopicsConstants.TOPIC_SUBSCRIBE) + TopicsConstants.RASPBERRY_AUTH_SUB + requestId, requestId};

            case TopicsConstants.SWITCH_ON_OFF_LIST_STATUS_SUB:
                return new String[]{getMQTTTopicPrefix(TopicsConstants.TOPIC_SUBSCRIBE) + TopicsConstants.SWITCH_ON_OFF_LIST_STATUS_SUB + requestId, requestId};

            case TopicsConstants.ACCESS_REVOCATION_SUB:
                return new String[]{getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + "SURVEILLANCE-V1/" + getMobileClientSecureElementId() + TopicsConstants.ACCESS_REVOCATION_SUB};

    }

        return null;
    }

    /**
     * Gets the prefix(subscribe/publish) of various topics
     */
    public static String getMQTTTopicPrefix(String type) {

        switch (type) {
            case TopicsConstants.TOPIC_PUBLISH:
                return "$EDC/" + "swissbit/" + getRaspberryPiById() + "/";

            case TopicsConstants.TOPIC_SUBSCRIBE:
                return "$EDC/" + "swissbit/" + getClientId() + "/";
        }

        return null;

    }

    /**
     * Gets the various topics to publish
     */
    public static String getTopicToPublish(String id) {

        switch (id) {

            case TopicsConstants.RASPBERRY_AUTH_PUB:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.RASPBERRY_AUTH_PUB;

            case TopicsConstants.SURVEILLANCE:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.SURVEILLANCE;

            case TopicsConstants.SWITCH_ON_PUB:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.SWITCH_ON_PUB;

            case TopicsConstants.SWITCH_OFF_PUB:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.SWITCH_OFF_PUB;

            case TopicsConstants.RETRIEVE_DEVICE_LIST_PUB:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.RETRIEVE_DEVICE_LIST_PUB;

            case TopicsConstants.RETRIEVE_DEVICE_STATUS_PUB:
                return getMQTTTopicPrefix(TopicsConstants.TOPIC_PUBLISH) + TopicsConstants.RETRIEVE_DEVICE_STATUS_PUB;


        }

        return null;
    }

    /**
     * Generates the Kura payload
     * Adds requestId and ClientId for every payload
     */
    public static KuraPayload generatePayload (String extraBody, String requestId) {
        final KuraPayload payload = new KuraPayload();

        payload.addMetric("request.id", requestId);
        payload.addMetric("requester.client.id", getClientId());
        payload.setBody(extraBody.getBytes());
        return payload;
    }

    /**
     * Checks if the mobile is connected to any network for communication
     */
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
