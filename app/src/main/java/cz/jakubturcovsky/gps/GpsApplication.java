package cz.jakubturcovsky.gps;

import android.app.Application;

public class GpsApplication
        extends Application {

    private static final String TAG = GpsApplication.class.getSimpleName();

    private static GpsApplication sInstance;

    public static GpsApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
