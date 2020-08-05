package android.example.vehiclemaintenancetracker.model;

import java.util.List;

public class ServiceNotificationCollection {
    private List<ServiceNotification> serviceNotifications;

    public List<ServiceNotification> getServiceNotifications() {
        return serviceNotifications;
    }

    public void setServiceNotifications(List<ServiceNotification> serviceNotifications) {
        this.serviceNotifications = serviceNotifications;
    }
}
