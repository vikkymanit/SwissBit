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
package com.swissbit.homeautomation.model;

/**
 * Model of the device table
 */
public class Device {

    /**
     * Node id of the device
     */
    private int deviceNodeId;

    /**
     * Id of the RaspberryPi
     */
    private String raspberryId;

    /**
     * Name of the device
     */
    private String name;

    /**
     * Description of the device
     */
    private String description;

    /**
     * Last successful status of the device
     */
    private String status;

    /**
     * Constructor
     */
    public Device(int deviceNodeId, String raspberryId, String name, String description, String status) {
        this.deviceNodeId = deviceNodeId;
        this.raspberryId = raspberryId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getDeviceNodeId() {
        return deviceNodeId;
    }

    public void setDeviceNodeId(int deviceNodeId) {
        this.deviceNodeId = deviceNodeId;
    }

    public String getRaspberryId() {
        return raspberryId;
    }

    public void setRaspberryId(String raspberryId) {
        this.raspberryId = raspberryId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Create a device
     */
    public static Device createDevice(final int id,final String raspberryId, final String name,final String description,final String status){
        return new Device(id, raspberryId, name, description, status);
    }
}
