package kushagra.com.shakeflashlight;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.TextView;

import java.security.Provider;

/**
 * Created by KUSHAGRA on 4/5/2016.
 */
public class ShakeService extends Service implements SensorEventListener {

    private static final String TAG = "HelloService";

    private boolean isRunning  = false;
    private long oldtime = 0;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;


    private static  int SHAKE_THRESHOLD = 1000;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    @Override
    public void onCreate() {
        //Log.i(TAG, "Service onCreate");


        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        isRunning = true;



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SHAKE_THRESHOLD = intent.getIntExtra("threshold",1000);

      //  Log.i(TAG, "Service onStartCommand");

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {


                //Your logic that service will perform will be placed here

                senSensorManager.registerListener(ShakeService.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            }
        }).start();

        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public IBinder onBind(Intent arg0) {
       // Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {

        isRunning = false;

       // Log.i(TAG, "Service onDestroy");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;




                double speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;


                speed = (long) (speed * 10000) / 10000.0;





                if (speed>SHAKE_THRESHOLD) {
                    // get the camera
                    getCamera();
                    long newtime = System.currentTimeMillis();
                    if(newtime-oldtime>2000) {

                        if (isFlashOn) {

                            // turn off flash
                            turnOffFlash();

                            camera.stopPreview();
                            camera.release();
                            camera = null;

                        } else {


                            // turn on flash
                            turnOnFlash();
                        }
                    }
                    oldtime=newtime;


                }


                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    // Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                //Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }


    // Turning On flash
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }


            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;


        }

    }


    // Turning Off flash
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }


            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;


        }
    }
}