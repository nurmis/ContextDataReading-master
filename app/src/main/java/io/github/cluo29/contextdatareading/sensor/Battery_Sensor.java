package io.github.cluo29.contextdatareading.sensor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import static org.mockito.Mockito.mock;

import io.github.cluo29.contextdatareading.sensor.Battery_Provider;
import io.github.cluo29.contextdatareading.sensor.Battery_Provider.Battery_Data;

public class Battery_Sensor {

    /**
     * Logging tag (default = "AWARE::Service")
     */
    public static String TAG = "AWARE::Battery";

    /**
     * Broadcasted event: the battery values just changed
     */
    public static final String ACTION_AWARE_BATTERY_CHANGED = "ACTION_AWARE_BATTERY_CHANGED";

    /**
     * Broadcasted event: the user just started charging
     */
    public static final String ACTION_AWARE_BATTERY_CHARGING = "ACTION_AWARE_BATTERY_CHARGING";

    /**
     * Broadcasted event: battery charging over power supply (AC)
     */
    public static final String ACTION_AWARE_BATTERY_CHARGING_AC = "ACTION_AWARE_BATTERY_CHARGING_AC";

    /**
     * Broadcasted event: battery charging over USB
     */
    public static final String ACTION_AWARE_BATTERY_CHARGING_USB = "ACTION_AWARE_BATTERY_CHARGING_USB";

    /**
     * Broadcasted event: the user just stopped charging and is running on battery
     */
    public static final String ACTION_AWARE_BATTERY_DISCHARGING = "ACTION_AWARE_BATTERY_DISCHARGING";

    /**
     * Broadcasted event: the battery is fully charged
     */
    public static final String ACTION_AWARE_BATTERY_FULL = "ACTION_AWARE_BATTERY_FULL";

    /**
     * Broadcasted event: the battery is running low and should be charged ASAP
     */
    public static final String ACTION_AWARE_BATTERY_LOW = "ACTION_AWARE_BATTERY_LOW";

    /**
     * Broadcasted event: the phone is about to be shutdown.
     */
    public static final String ACTION_AWARE_PHONE_SHUTDOWN = "ACTION_AWARE_PHONE_SHUTDOWN";

    /**
     * Broadcasted event: the phone is about to be rebooted.
     */
    public static final String ACTION_AWARE_PHONE_REBOOT = "ACTION_AWARE_PHONE_REBOOT";

    /**
     * {@link Battery_Data#STATUS} Phone shutdown
     */
    public static final int STATUS_PHONE_SHUTDOWN = -1;

    /**
     * {@link Battery_Data#STATUS} Phone rebooted
     */
    public static final int STATUS_PHONE_REBOOT = -2;

    public static class Battery_Service extends Service {
        private final IBinder OnReceiveBinder = new OnReceiveBinder();

        public class OnReceiveBinder extends Binder {
            public Battery_Service getService(){
                return Battery_Service.this;
            }
        }

        public void onReceive(Context context, Intent intent) {

            if(intent.getBooleanExtra("FAKEDATA", false) == true) {
                Log.d("Tester", "goes to fakedata");
                Bundle extras = intent.getExtras();

                ContentValues rowData = new ContentValues();
                Log.d("Tester", extras.toString());

                rowData.put(Battery_Data.TIMESTAMP, extras.getLong("TIMESTAMP"));
                rowData.put(Battery_Data.DEVICE_ID, extras.getString("DEVICE_ID"));
                rowData.put(Battery_Data.STATUS, extras.getInt("STATUS"));
                rowData.put(Battery_Data.LEVEL, extras.getInt("LEVEL"));
                rowData.put(Battery_Data.SCALE, extras.getInt("SCALE"));
            }

           if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                Log.d("Tester", "goes to usual if");
                Bundle extras = intent.getExtras();
                if (extras == null) return;

                ContentValues rowData = new ContentValues();
                rowData.put(Battery_Data.TIMESTAMP, System.currentTimeMillis());
                rowData.put(Battery_Data.DEVICE_ID, "");
                rowData.put(Battery_Data.STATUS, extras.getInt(BatteryManager.EXTRA_STATUS));
                rowData.put(Battery_Data.LEVEL, extras.getInt(BatteryManager.EXTRA_LEVEL));
                rowData.put(Battery_Data.SCALE, extras.getInt(BatteryManager.EXTRA_SCALE));
                rowData.put(Battery_Data.VOLTAGE, extras.getInt(BatteryManager.EXTRA_VOLTAGE));
                rowData.put(Battery_Data.TEMPERATURE, extras.getInt(BatteryManager.EXTRA_TEMPERATURE) / 10);
                rowData.put(Battery_Data.PLUG_ADAPTOR, extras.getInt(BatteryManager.EXTRA_PLUGGED));
                rowData.put(Battery_Data.HEALTH, extras.getInt(BatteryManager.EXTRA_HEALTH));
                rowData.put(Battery_Data.TECHNOLOGY, extras.getString(BatteryManager.EXTRA_TECHNOLOGY));

                try {
                    //if (Aware.DEBUG) Log.d(TAG, "Battery:" + rowData.toString());
                    context.getContentResolver().insert(Battery_Data.CONTENT_URI, rowData);
                } catch (SQLiteException e) {
                    //if (Aware.DEBUG) Log.d(TAG, e.getMessage());
                } catch (SQLException e) {
                    //if (Aware.DEBUG) Log.d(TAG, e.getMessage());
                }

                if (extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_AC) {
                    //if (Aware.DEBUG) Log.d(TAG, ACTION_AWARE_BATTERY_CHARGING_AC);
                    Intent battChargeAC = new Intent(ACTION_AWARE_BATTERY_CHARGING_AC);
                    context.sendBroadcast(battChargeAC);
                }

                if (extras.getInt(BatteryManager.EXTRA_PLUGGED) == BatteryManager.BATTERY_PLUGGED_USB) {
                    //if (Aware.DEBUG) Log.d(TAG, ACTION_AWARE_BATTERY_CHARGING_USB);
                    Intent battChargeUSB = new Intent(ACTION_AWARE_BATTERY_CHARGING_USB);
                    context.sendBroadcast(battChargeUSB);
                }

                if (extras.getInt(BatteryManager.EXTRA_STATUS) == BatteryManager.BATTERY_STATUS_FULL) {
                    //if (Aware.DEBUG) Log.d(TAG, ACTION_AWARE_BATTERY_FULL);
                    Intent battFull = new Intent(ACTION_AWARE_BATTERY_FULL);
                    context.sendBroadcast(battFull);
                }

                //if (Aware.DEBUG) Log.d(TAG, ACTION_AWARE_BATTERY_CHANGED);
                Intent battChanged = new Intent(ACTION_AWARE_BATTERY_CHANGED);
                context.sendBroadcast(battChanged);
                }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return OnReceiveBinder;
        }
    }

    private static final Battery_Service batteryMonitor = new Battery_Service();

    /**
     * Activity-Service binder
     */
    private final IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        Battery_Sensor getService() {
            return Battery_Sensor.getService();
        }
    }


    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    private static Battery_Sensor batterySrv = Battery_Sensor.getService();
    /**
     * Singleton instance to service
     * @return Battery
     */
    public static Battery_Sensor getService() {
        if( batterySrv == null ) batterySrv = new Battery_Sensor();
        return batterySrv;
    }


    public void onCreate() {
        //super.onCreate();

        //TAG = Aware.getSetting(getApplicationContext(), Aware_Preferences.DEBUG_TAG).length()>0?Aware.getSetting(getApplicationContext(), Aware_Preferences.DEBUG_TAG):TAG;

        //DATABASE_TABLES = Battery_Provider.DATABASE_TABLES;
        //TABLES_FIELDS = Battery_Provider.TABLES_FIELDS;
        //CONTEXT_URIS = new Uri[]{ Battery_Data.CONTENT_URI, null, null };

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        //filter.addAction(Intent.ACTION_BATTERY_LOW);
        //filter.addAction(Intent.ACTION_SHUTDOWN);
        //filter.addAction(Intent.ACTION_REBOOT);
        //filter.addAction(Intent.ACTION_POWER_CONNECTED);
        //filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        //registerReceiver(batteryMonitor, filter);

        //if(Aware.DEBUG) Log.d(TAG, "Battery service created!");
    }

    public void onDestroy() {
        //super.onDestroy();

        //unregisterReceiver(batteryMonitor);

        //if(Aware.DEBUG) Log.d(TAG,"Battery service terminated...");
    }
}
