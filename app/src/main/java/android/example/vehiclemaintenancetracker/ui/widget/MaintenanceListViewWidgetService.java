package android.example.vehiclemaintenancetracker.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class MaintenanceListViewWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MaintenanceListRemoteViewsFactory(getApplicationContext(), intent);
    }
}
