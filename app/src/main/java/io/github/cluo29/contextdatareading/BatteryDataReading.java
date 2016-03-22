package io.github.cluo29.contextdatareading;


import io.github.cluo29.contextdatareading.noisiness.*;

import io.github.cluo29.contextdatareading.semantization.parser.*;
import io.github.cluo29.contextdatareading.sensor.Battery_Sensor;
import io.github.cluo29.contextdatareading.table.Battery;

import android.app.Activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import org.mockito.Mockito;
import android.os.BatteryManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.Format;
import java.util.Objects;
import java.util.UUID;

import javax.xml.datatype.DatatypeConstants;


/**
 * Created by Comet on 16/03/16.
 */
public class BatteryDataReading extends Service {
    Battery_Sensor.Battery_Service br;
    boolean bound = false;

    private ServiceConnection brConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Battery_Sensor.Battery_Service.OnReceiveBinder binder = (Battery_Sensor.Battery_Service.OnReceiveBinder) service;
            br = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void onCreate() {
        Log.d("Tester", "oncreate");
        Intent intent = new Intent(this, Battery_Sensor.Battery_Service.class);
        bindService(intent, brConnection, Context.BIND_AUTO_CREATE);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {

            final DataSource ds = new MysqlDataSource("awareframework.com", 3306, "Luo_661", "", "Luo_661");
            final DataNoiser dn = new SimpleDataNoiser(1391062684000L, 3600000L, 0.3, 0.1, 0.4);
            final AwareSimulator sim = new AwareSimulator(ds, dn, 1458126481750L, UUID.fromString("83bc93f4-e631-4007-b87f-9f0e47669537"));
            sim.setSpeed(100.0);
            sim.battery.addListener(new AwareSimulator.Listener<Battery>() {
                public void onEvent(Battery event) {
                    //Log.d("Tester", "Battery: " + event);
                    Intent intent = new Intent("ACTION_BATTERY_CHANGED");

                    intent.putExtra("FAKEDATA", true);
                    intent.putExtra("TIMESTAMP", event.timestamp());
                    intent.putExtra("DEVICE_ID", Objects.toString(event.device()));
                    intent.putExtra("STATUS", event.batteryStatus);
                    intent.putExtra("LEVEL", event.batteryLevel);
                    intent.putExtra("SCALE", event.batteryScale);

                    if (bound) {
                        br.onReceive(getApplicationContext(), intent);
                    }
                }
            });
            sim.start();

        } catch (SQLException|ClassNotFoundException e) {
            Log.d("Tester", "48");
            e.printStackTrace();

        }
    }

    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /*public BatteryManager CreateBatteryManager(Intent dataIntent) throws Exception{
        Log.d("Tester", "goes to createbatterymanager");

        // CRASHES THE WHOLE THING
        BatteryManager fakeManager = Mockito.mock(BatteryManager.class);

        /*Bundle data = dataIntent.getExtras();
        Log.d("Tester", "before try");
        try{
            Field status = BatteryManager.class.getField("EXTRA_STATUS");
            Log.d("Tester", "before set accessible");
            status.setAccessible(true);
            try {
                Log.d("Tester", "before status.set");
                status.set(BatteryManager.EXTRA_STATUS, data.getString("STATUS"));
            }catch (IllegalAccessException e){
                Log.d("Tester", "Illegal access");
            }
            Log.d("Tester", "before set accessible");
        }catch (NoSuchFieldException e){
            Log.d("Tester", "nosuchfield");
        }


        return fakeManager;
    }*/

}
