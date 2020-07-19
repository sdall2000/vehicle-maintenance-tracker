package android.example.vehiclemaintenancetracker.model;

import java.util.Date;

public class ServiceNotification {
    private String service;
    private Integer mileageDue;
    private Date dateDue;
    private Status status;

    public ServiceNotification(String service, Integer mileageDue, Date dateDue, Status status) {
        this.service = service;
        this.mileageDue = mileageDue;
        this.dateDue = dateDue;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }
}
