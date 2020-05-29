package android.example.vehiclemaintenancetracker.model;

import java.util.Date;

public class ServiceNotification {
    private String service;
    private Integer mileageDue;
    private Date dateDue;

    public ServiceNotification(String service, Integer mileageDue, Date dateDue) {
        this.service = service;
        this.mileageDue = mileageDue;
        this.dateDue = dateDue;
    }

    public String getService() {
        return service;
    }

    public Integer getMileageDue() {
        return mileageDue;
    }

    public Date getDateDue() {
        return dateDue;
    }
}
