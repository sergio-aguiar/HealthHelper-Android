package pt.ua.aguiar.sergio.healthhelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Objects;

import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperMarker;

public class HealthHelperPastDataPage extends Activity {

    ImageButton appBackButton;

    LinearLayout bottomBarButtonLeft;
    LinearLayout bottomBarButtonRight;

    ArrayList<DataPoint> graphData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_helper_past_data_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        findViewById(R.id.past_data_page_constraint_layout).setBackgroundResource(R.drawable.tween_gradient_animation);

        appBackButton = findViewById(R.id.top_app_bar_back);

        appBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HealthHelperMainMenuPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        bottomBarButtonLeft = findViewById(R.id.past_data_to_tracking_layout);
        bottomBarButtonRight = findViewById(R.id.past_data_to_favorites_layout);

        bottomBarButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperTrackingPage.class));
            }
        });

        bottomBarButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperFavoritesPage.class));
            }
        });

        graphData = new ArrayList<>();

        final GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.INVISIBLE);

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("times")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            int count = 0;
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                graphData.add(new DataPoint(count++, (long) document.get("time")));
                            }

                            DataPoint[] dataPoints = new DataPoint[graphData.size()];
                            for(int i = 0; i < dataPoints.length; i++) {
                                dataPoints[i] = graphData.get(i);
                            }

                            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
                            graph.addSeries(series);
                            graph.setVisibility(View.VISIBLE);
                        } else {
                            System.out.println("Failed to fetch data!");
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), HealthHelperMainMenuPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        AnimationDrawable gradientAnimation = (AnimationDrawable) findViewById(R.id.past_data_page_constraint_layout).getBackground();
        gradientAnimation.setEnterFadeDuration(30);
        gradientAnimation.setExitFadeDuration(2000);
        gradientAnimation.setVisible(true, true);
        gradientAnimation.start();
    }

}
