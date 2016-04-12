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
 * Constants for the webservice
 */
public interface WSConstants {

    /**
     * Webservice url for getting the credentials from the broker
     */
    public static final String CREDENTIAL_WS = "http://andropraktikumtum-agms.rhcloud.com/user/";

    /**
     * Webservice url for updating that the RaspberryPi has been validated.
     * This is used for the swissbit administration.
     */
    public static final String ADD_RPI_WS = "http://andropraktikumtum-agms.rhcloud.com/addPi/";

    String[] secureIds = {"1b58095eb4c6b36d794c3ed776ae2378","23c468ce274a35380fb758b406421d16"};
}
