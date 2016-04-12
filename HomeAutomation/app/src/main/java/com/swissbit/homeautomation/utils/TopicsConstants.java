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
 * Constants for various publish and subscribe topics
 */
public interface TopicsConstants {

    /**
     * Part of topic for RaspberryPi failure status
     */
    public static final String LWT = "MQTT/LWT";

    /**
     * Part of topic for RaspberryPi heartbeat
     */
    public static final String HEARTBEAT = "HEARTBEAT-V1/";

    /**
     * Part of topic for all publish topic
     */
    public static final String TOPIC_PUBLISH = "PUB";

    /**
     * Part of topic for all subscribe topic
     */
    public static final String TOPIC_SUBSCRIBE = "SUB";

    /**
     * Part of publish topic for RaspberryPi Authentication
     */
    public static final String RASPBERRY_AUTH_PUB = "AUTH-V1/EXEC/decrypt";

    /**
     * Part of subscribe topic for RaspberryPi Authentication
     */
    public static final String RASPBERRY_AUTH_SUB = "AUTH-V1/REPLY/";

    /**
     * Part of subscribe topic for RaspberryPi Surveillance.
     */
    public static final String SURVEILLANCE = "SURVEILLANCE-V1/POST/sample";

    /**
     * Part of publish topic for Device on command
     */
    public static final String SWITCH_ON_PUB = "DEVICE-V1/EXEC/on";

    /**
     * Part of publish topic for Device off command
     */
    public static final String SWITCH_OFF_PUB = "DEVICE-V1/EXEC/off";

    /**
     * Part of subscribe topic for Device on/off and refresh command
     */
    public static final String SWITCH_ON_OFF_LIST_STATUS_SUB = "DEVICE-V1/REPLY/";

    /**
     * Part of publish topic for Device list retrieval
     */
    public static final String RETRIEVE_DEVICE_LIST_PUB = "DEVICE-V1/GET/list";

    /**
     * Part of publish topic for Device status retrieval
     */
    public static final String RETRIEVE_DEVICE_STATUS_PUB = "DEVICE-V1/GET/status";

    /**
     * Part of subscribe topic for access revocation
     */
    public static final String ACCESS_REVOCATION_SUB = "/access/revoked";

    /**
     * Part of subscribe topic for secure chat
     */
    public static final String SECURE_CHAT = "$EDC/secure/chat";

}
