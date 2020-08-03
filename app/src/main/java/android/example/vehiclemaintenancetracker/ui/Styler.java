package android.example.vehiclemaintenancetracker.ui;

import android.content.Context;
import android.example.vehiclemaintenancetracker.R;
import android.example.vehiclemaintenancetracker.model.Status;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

public class Styler {
    public static void styleTextViewStatus(Context context, TextView textView, Status status) {
        switch (status) {
            case Overdue:
                textView.setBackgroundColor(getStatusColor(context, status));
                break;
            case Upcoming:
                textView.setBackgroundColor(getStatusColor(context, status));
                break;
            case Good:
                // Let's not set any background if the status is good.
                break;
        }
    }

    public static void styleImageViewStatus(Context context, ImageView imageView, Status status) {
        switch (status) {
            case Overdue:
                imageView.setImageResource(R.drawable.ic_overdue);
                imageView.setContentDescription(context.getString(R.string.service_overdue));
                break;
            case Upcoming:
                imageView.setImageResource(R.drawable.ic_upcoming);
                imageView.setContentDescription(context.getString(R.string.service_upcoming));
                break;
            case Good:
                // Don't set any image if the status is good.
                break;
        }
    }

    public static int getStatusColor(Context context, Status status) {
        int color = 0;

        switch (status) {
            case Overdue:
                color = context.getResources().getColor(R.color.statusOverdueBackground);
                break;
            case Upcoming:
                color = context.getResources().getColor(R.color.statusUpcomingBackground);
                break;
            case Good:
                color = context.getResources().getColor(R.color.statusGoodBackground);
                break;
        }

        return color;
    }

    public static void styleResourceStatus(Context context, RemoteViews remoteViews, int resourceId, Status status) {
        // Only set background color if the status is not good.
        if (status != Status.Good) {
            remoteViews.setInt(resourceId, "setBackgroundColor", getStatusColor(context, status));
        }
    }

    public static void styleImageResourceStatus(Context context, RemoteViews remoteViews, int resourceId, Status status) {
        // Only set background color if the status is not good.
        if (status != Status.Good) {
            remoteViews.setInt(resourceId, "setBackgroundColor", getStatusColor(context, status));
        }
    }
}
