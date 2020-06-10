package pt.ua.aguiar.sergio.healthhelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperMarker;
import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperMarkerList;
import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnMarkerListChangeListener;

public class HealthHelperFavoritesPage extends Activity {

    ImageButton appBackButton;

    LinearLayout bottomBarButtonLeft;
    LinearLayout bottomBarButtonRight;

    HorizontalScrollView scrollView;

    HealthHelperMarkerList healthHelperMarkerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_helper_favorites_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        findViewById(R.id.favorites_page_constraint_layout).setBackgroundResource(R.drawable.tween_gradient_animation);

        appBackButton = findViewById(R.id.top_app_bar_back);

        appBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HealthHelperMainMenuPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        bottomBarButtonLeft = findViewById(R.id.favorites_to_tracking_layout);
        bottomBarButtonRight = findViewById(R.id.favorites_to_past_data_layout);

        bottomBarButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperTrackingPage.class));
            }
        });

        bottomBarButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperPastDataPage.class));
            }
        });

        healthHelperMarkerList = new HealthHelperMarkerList();
        healthHelperMarkerList.setOnMarkerListChangeListener(new OnMarkerListChangeListener() {
            @Override
            public void onMarkerListChangeListener(ArrayList<HealthHelperMarker> healthHelperMarkers) {
                System.out.println(healthHelperMarkerList);
            }
        });

        scrollView = findViewById(R.id.favorite_list);

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("markerInfo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                healthHelperMarkerList.addMarker(new HealthHelperMarker(
                                        Objects.requireNonNull(document.get("markerID")).toString(),
                                        new LatLng(
                                                (double) document.get("latitude"),
                                                (double) document.get("longitude")
                                        ),
                                        (String) document.get("imagePath")
                                ));
                            }

                            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
                            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                            for(HealthHelperMarker marker : healthHelperMarkerList.getHealthHelperMarkers()) {
                                ImageView tmp = new ImageView(getApplicationContext());
                                tmp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                tmp.setMaxWidth(300);

                                File imgFile = new File(marker.getImagePath());
                                Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                tmp.setImageBitmap(imgBitmap);

                                linearLayout.setGravity(Gravity.START);
                                linearLayout.addView(tmp);
                            }
                            scrollView.addView(linearLayout);
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

        AnimationDrawable gradientAnimation = (AnimationDrawable) findViewById(R.id.favorites_page_constraint_layout).getBackground();
        gradientAnimation.setEnterFadeDuration(30);
        gradientAnimation.setExitFadeDuration(2000);
        gradientAnimation.setVisible(true, true);
        gradientAnimation.start();
    }

}
