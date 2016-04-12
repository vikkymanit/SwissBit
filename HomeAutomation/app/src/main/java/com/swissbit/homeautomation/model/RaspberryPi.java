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
 * Model of the Raspberry table
 */
public final class RaspberryPi {

    /**
     * Id of the RaspberryPi
     */
    private String id;

    /**
     * Id of the secure element (SD card)
     */
    private String secureElementId;

    /**
     * status of the RaspberryPi
     */
    private boolean status;

    /**
     * Description of the RaspberryPi
     */
    private String description;

    /**
     * Name of the RaspberryPi
     */
    private String name;

    /**
     * Constructor
     */
    public RaspberryPi(String id, String secureElementId, String description, String name, boolean status) {
        this.id = id;
        this.secureElementId = secureElementId;
        this.description = description;
        this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getSecureElementId() {
        return secureElementId;
    }

    public void setSecureElementId(String secureElementId) {
        this.secureElementId = secureElementId;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Create a RaspberryPi
     */
    public static RaspberryPi createRaspberryPi(final String id, final String secureElementId, final String name, final String desc, final boolean status){
        return new RaspberryPi(id, secureElementId, desc, name, status);
    }

}
