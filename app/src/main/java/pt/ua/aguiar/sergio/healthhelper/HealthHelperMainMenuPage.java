package pt.ua.aguiar.sergio.healthhelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcinmoskala.arcseekbar.ArcSeekBar;

import java.util.HashMap;
import java.util.Objects;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnTimesChangeListener;
import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperMarker;
import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperTimes;

import java.util.Timer;
import java.util.TimerTask;

public class HealthHelperMainMenuPage extends Activity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    ImageButton appBackButton;

    ArcSeekBar arcSeekBar;

    TextView sliderText;
    TextView movingText;
    TextView stoppedText;

    RelativeLayout trackingLayout;
    RelativeLayout favoritesLayout;
    RelativeLayout pastDataLayout;

    HealthHelperTimes healthHelperTimes;

    boolean loaded;
    static Timer timer;
    static int walkingTime = 0;
    static int runningTime = 0;
    static int stoppedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_helper_main_menu_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        findViewById(R.id.main_menu_page_constraint_layout).setBackgroundResource(R.drawable.tween_gradient_animation);

        appBackButton = findViewById(R.id.top_app_bar_back);

        appBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HealthHelperMainPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        loaded = false;

        arcSeekBar = findViewById(R.id.seekArc);
        int[] colorArray = getResources().getIntArray(R.array.seek_arc_gradient);
        arcSeekBar.setProgressBackgroundGradient(colorArray);

        trackingLayout = findViewById(R.id.tracking_layout);
        favoritesLayout = findViewById(R.id.favorites_layout);
        pastDataLayout = findViewById(R.id.past_data_layout);

        trackingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperTrackingPage.class));
            }
        });

        favoritesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperFavoritesPage.class));
            }
        });

        pastDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperPastDataPage.class));
            }
        });

        sliderText = findViewById(R.id.slider_value_text);
        movingText = findViewById(R.id.moving_dynamic_text);
        stoppedText = findViewById(R.id.stopped_dynamic_text);

        healthHelperTimes = new HealthHelperTimes();
        healthHelperTimes.setChangeListener(new OnTimesChangeListener() {
            @Override
            public void onTimesChangeListener(int moving, int stopped) {
                loaded = true;
            }
        });

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("times")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                if((long) document.get("time") == 0) healthHelperTimes.incrementStopped();
                                else healthHelperTimes.incrementMoving();
                            }
                        } else {
                            System.out.println("Failed to fetch data!");
                        }
                    }
                });

        timer = new Timer();
        timer.scheduleAtFixedRate(new SensorProcessingTask(), 0,1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAuth.signOut();
        Intent intent = new Intent(getApplicationContext(), HealthHelperMainPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        AnimationDrawable gradientAnimation = (AnimationDrawable) findViewById(R.id.main_menu_page_constraint_layout).getBackground();
        gradientAnimation.setEnterFadeDuration(30);
        gradientAnimation.setExitFadeDuration(2000);
        gradientAnimation.setVisible(true, true);
        gradientAnimation.start();
    }

    class SensorProcessingTask extends TimerTask  implements SensorEventListener {

        private ArcSeekBar arc_seek_bar;

        private TextView sliderText;
        private TextView moving_dynamic_text;
        private TextView stopped_dynamic_text;

        private int movement = 0;

        private int stepsCount = 0;
        private int stepsCount1 = 0;

        SensorProcessingTask() {
            arc_seek_bar = findViewById(R.id.seekArc);

            sliderText = findViewById(R.id.slider_value_text);
            moving_dynamic_text = findViewById(R.id.moving_dynamic_text);
            stopped_dynamic_text = findViewById(R.id.stopped_dynamic_text);

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            assert sensorManager != null;
            Sensor steps = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            Sensor acceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            sensorManager.registerListener(this, steps, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()){
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    if(stepsCount1 == stepsCount){
                        if(x > 0.5 || y > 0.5 || z > 0.5) {
                            movement++;
                        }
                    }
                    stepsCount1 = stepsCount;
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    float steps = event.values[0];
                    stepsCount = (int) steps;
                    break;
                default:
            }

            if(loaded) {
                int barPos = (int) HealthHelperTimes.calcBarPosition(healthHelperTimes.getMoving() + walkingTime, healthHelperTimes.getStopped() + stoppedTime);
                arcSeekBar.setProgress(barPos);
                sliderText.setText(HealthHelperTimes.calcBarText(barPos));
                sliderText.setTextColor(HealthHelperTimes.calcTextColor(barPos));
                moving_dynamic_text.setText(HealthHelperTimes.fromIntTimeToStringTime(healthHelperTimes.getMoving() + walkingTime));
                stopped_dynamic_text.setText(HealthHelperTimes.fromIntTimeToStringTime(healthHelperTimes.getStopped() + stoppedTime));
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        public void run() {
            int timeValue = -1;

            if (movement >= 3) {
                walkingTime++;
                timeValue = 1;
            }
            else {
                stoppedTime++;
                timeValue = 0;
            }

            HashMap<String, Object> times = new HashMap<>();
            times.put("timestamp",System.currentTimeMillis());
            times.put("time", timeValue);

            FirebaseFirestore.getInstance()
                    .collection("HealthHelper")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .collection("times")
                    .document()
                    .set(times);

            movement = 0;
        }
    }
}
