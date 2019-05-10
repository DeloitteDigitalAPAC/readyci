package com.squarepolka.readyci.tasks.app.ios.provisioningprofile;

public class ProvisioningProfile {
    private String name;
    private String bundleId;
    private boolean hasProvisionedDevices;

    public ProvisioningProfile(String name, String bundleId, boolean hasProvisionedDevices) {
        this.name = name;
        this.bundleId = bundleId;
        this.hasProvisionedDevices = hasProvisionedDevices;
    }

    public String provisioningType(String fullBundleId) {
        if (hasProvisionedDevices) {
            return "ad-hoc";
        } else if (fullBundleId.endsWith("*")){
            return "enterprise";
        } else {
            return "app-store";
        }
    }

    public String getName() {
        return name;
    }

    public String getBundleId() {
        return bundleId;
    }

}
