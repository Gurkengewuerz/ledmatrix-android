package de.hochschule_bochum.blootothcontroller;

/**
 * Created by nikla on 08.07.2017.
 */

public class BTDevice {
    private String deviceName;
    private String mac;
    private boolean selected;

    public BTDevice(String deviceName, String mac) {
        this.deviceName = deviceName;
        this.mac = mac;
    }


    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMac() {
        return mac;
    }

    public boolean isSelected() {
        return selected;
    }

}
