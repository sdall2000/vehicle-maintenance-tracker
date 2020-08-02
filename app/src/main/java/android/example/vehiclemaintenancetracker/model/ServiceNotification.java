package android.example.vehiclemaintenancetracker.model;

import java.util.Date;

public class ServiceNotification {
    private final String service;
    private final Integer mileageDue;
    private final Date dateDue;
    private final Status mileageStatus;
    private final Status dateStatus;
    private final Status overallStatus;

    public ServiceNotification(
            String service,
            Integer mileageDue,
            Date dateDue,
            Status mileageStatus,
            Status dateStatus,
            Status overallStatus) {
        this.service = service;
        this.mileageDue = mileageDue;
        this.dateDue = dateDue;
        this.mileageStatus = mileageStatus;
        this.dateStatus = dateStatus;
        this.overallStatus = overallStatus;
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

    public Status getMileageStatus() {
        return mileageStatus;
    }

    public Status getDateStatus() {
        return dateStatus;
    }

    public Status getOverallStatus() {
        return overallStatus;
    }
}
