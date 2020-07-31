package android.example.vehiclemaintenancetracker.utilities;

import java.text.DecimalFormat;

public class ValueFormatter {
    private static final DecimalFormat distanceFormat = new DecimalFormat("#,###");
    private static final DecimalFormat costFormat = new DecimalFormat("$#,###.00");

    public static String formatDistance(int distance) {
        return distanceFormat.format(distance);
    }

    public static String formatCost(double cost) {
        return costFormat.format(cost);
    }
}
