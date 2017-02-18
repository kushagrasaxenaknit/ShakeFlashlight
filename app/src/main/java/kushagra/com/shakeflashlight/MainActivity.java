package kushagra.com.shakeflashlight;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


        private SensorManager senSensorManager;
        private Sensor senAccelerometer;
    boolean dialogflag=true;

    private long lastUpdate = 0;
    private long oldtime = 0;
    private float last_x, last_y, last_z;
    private static  int SHAKE_THRESHOLD = 1000;
    //private static  int SHAKE_THRESHOLD = 15;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SeekBar seekBar;

    private Switch aSwitch ;



        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.content_main);

            seekBar = (SeekBar) findViewById(R.id.iseekBar);
            aSwitch =(Switch)findViewById(R.id.switch1);


            senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            // First check if device is supporting flashlight or not
            hasFlash = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


            //attach a listener to check for changes in state
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    if(isChecked&&(!isFlashOn)){
                        getCamera();
                        // turn on flash
                        turnOnFlash();


                    }
                    if((!isChecked)&&(isFlashOn))
                    {
                        // turn off flash
                        turnOffFlash();


                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }

                }
            });
            //check the current state before we display the screen
            if(aSwitch.isChecked()&&(!isFlashOn)){
                getCamera();
                // turn on flash
                turnOnFlash();


            }
            if((!aSwitch.isChecked())&&(isFlashOn))
            {
                // turn off flash
                turnOffFlash();


                camera.stopPreview();
                camera.release();
                camera = null;
            }


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {


                    if (progress>1500&&dialogflag) {

                        showAlertDialog(MainActivity.this,"Sensivity" ,
                                "Flashlight might turn on-off on high sensivity so choose a proper value",
                                 false);
                        dialogflag=false;
                    }


                    if (progress > 1000) {

                        SHAKE_THRESHOLD = 1000 - progress / 10;
                    } else if (progress < 1000) {
                        SHAKE_THRESHOLD = 1000 + progress / 5;
                    } else {
                        SHAKE_THRESHOLD = 1000;
                    }


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            if (!hasFlash) {
                // device doesn't support flash
                // Show alert message and close the application
                AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                        .create();
                alert.setTitle("Error");
                alert.setMessage("Sorry, your device doesn't support flash light!");
                alert.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // closing the application
                        finish();
                    }
                });
                alert.show();
                return;
            }


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


                    //double speed = (Math.abs( y -last_y ))/ diffTime ;
                    double speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;


                    speed = (long) (speed * 10000) / 10000.0;






                    if (speed>SHAKE_THRESHOLD) {
                        // get the camera
                        getCamera();
                        long newtime = System.currentTimeMillis();
                        if(newtime-oldtime>2000) {


                        if(isFlashOn)
                        {


                                aSwitch.setChecked(false);





                        }
                        else {


                            // turn on flash


                                aSwitch.setChecked(true);




                        }
                            oldtime=newtime;
                        }




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
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
        alertDialog.setIcon( R.drawable.flash );
        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // Showing Alert Message
        alertDialog.show();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start) {

            if(isMyServiceRunning(ShakeService.class))
            {
                Toast.makeText(getApplicationContext(),"Already Running",Toast.LENGTH_LONG).show();
                return true;
            }

            Intent intent = new Intent(this, ShakeService.class);
            intent.putExtra("threshold",SHAKE_THRESHOLD);

            startService(intent);
            Toast.makeText(getApplicationContext(),"Started",Toast.LENGTH_LONG).show();
            return true;

        }
        if (id == R.id.action_stop) {
            if(isMyServiceRunning(ShakeService.class))
            {



                Intent intent = new Intent(this, ShakeService.class);
                stopService(intent);

                Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_LONG).show();
                return true;

            }

            Toast.makeText(getApplicationContext(),"Service not Running",Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_developer) {
            Intent intent = new Intent(this, Main2Activity.class);
            intent.putExtra("shake", SHAKE_THRESHOLD);
            startActivity(intent);






            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
     protected void onStart() {
        super.onStart();

        // on starting the app get the camera params

    }


}
