package cz.jakubturcovsky.gps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cz.jakubturcovsky.gps.helper.PreferencesHelper;

public class LocationService
        extends Service {

    private static final String TAG = LocationService.class.getSimpleName();

    public static final long DEFAULT_ACQUIRE_LOCATION_PERIOD = 300_000L;      // 5 min * 60s * 1000ms

    private ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture mAcquireLocationFuture;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Yes, I could use Extras. This, however, seems to be a bit more universal.
        resetAcquireLocationTimer(PreferencesHelper.getAcquireLocationPeriod());
        return START_STICKY;
    }

    private void resetAcquireLocationTimer(long period) {
        if (period == -1) {
            scheduleLocationTimer(DEFAULT_ACQUIRE_LOCATION_PERIOD);
        } else if (period == 0) {
            cancelLocationFuture();
        } else {
            scheduleLocationTimer(period);
        }
    }

    private void scheduleLocationTimer(long period) {
        cancelLocationFuture();
        mAcquireLocationFuture = mExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "Acquiring location");
            }
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    private void cancelLocationFuture() {
        if (mAcquireLocationFuture != null) {
            mAcquireLocationFuture.cancel(true);
            mAcquireLocationFuture = null;
        }
    }
}
