package android.example.vehiclemaintenancetracker.model;

import java.util.Date;

public class History {
    private Date date;
    private int mileage;
    private String service;
    private double cost;
    private String provider;

    public History(Date date, int mileage, String service, double cost, String provider) {
        this.date = date;
        this.mileage = mileage;
        this.service = service;
        this.cost = cost;
    }

    public Date getDate() {
        return date;
    }

    public int getMileage() {
        return mileage;
    }

    public String getService() {
        return service;
    }

    public double getCost() {
        return cost;
    }

    public String getProvider() {
        return provider;
    }
}
