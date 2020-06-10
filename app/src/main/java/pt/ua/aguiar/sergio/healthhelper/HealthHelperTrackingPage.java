package pt.ua.aguiar.sergio.healthhelper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnLatLngChangeListener;
import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperPolyPoints;

public class HealthHelperTrackingPage extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private ImageButton appBackButton;

    private Button addMarkerButton;

    private LinearLayout bottomBarButtonLeft;
    private LinearLayout bottomBarButtonRight;

    private MapView mapView;
    private GoogleMap gmap;

    LocationManager locationManager;

    private boolean mapPositionInit;
    private LatLng lastLocation;

    private HealthHelperPolyPoints healthHelperPolyPoints;
    private Polyline currentPolyline;
    private Polyline oldPolyline;
    private ArrayList<LatLng> currentPoints;
    private ArrayList<LatLng> oldPoints;

    static final int REQUEST_TAKE_PHOTO = 1;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_helper_tracking_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        findViewById(R.id.tracking_page_constraint_layout).setBackgroundResource(R.drawable.tween_gradient_animation);

        appBackButton = findViewById(R.id.top_app_bar_back);

        appBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HealthHelperMainMenuPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        bottomBarButtonLeft = findViewById(R.id.tracking_to_favorites_layout);
        bottomBarButtonRight = findViewById(R.id.tracking_to_past_data_layout);

        bottomBarButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperFavoritesPage.class));
            }
        });

        bottomBarButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HealthHelperPastDataPage.class));
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 1
            );
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        mapPositionInit = false;
        lastLocation = new LatLng(32.0811895, 2.6438951);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }

        mapView = findViewById(R.id.tracking_map);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        addMarkerButton = findViewById(R.id.add_marker);

        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    File file = null;

                    try {
                        file = createImageFile();
                    }
                    catch (IOException ignored) {}

                    if (file != null) {
                        v.getContext().getPackageName();
                        Uri photoURI = FileProvider.getUriForFile(v.getContext(), "zoooooooooooooom", file);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

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
                                gmap.addMarker(new MarkerOptions()
                                    .position(new LatLng(
                                            (double) document.get("latitude"),
                                            (double) document.get("longitude")))
                                );
                            }
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

        AnimationDrawable gradientAnimation = (AnimationDrawable) findViewById(R.id.tracking_page_constraint_layout).getBackground();
        gradientAnimation.setEnterFadeDuration(30);
        gradientAnimation.setExitFadeDuration(2000);
        gradientAnimation.setVisible(true, true);
        gradientAnimation.start();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle("MapViewBundleKey");
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle("MapViewBundleKey", mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gmap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 1
            );
            return;
        }

        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setMinZoomPreference(2);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setCompassEnabled(true);

        CameraPosition.Builder camBuilder = CameraPosition.builder();
        camBuilder.target(lastLocation);
        camBuilder.zoom(2);

        CameraPosition cp = camBuilder.build();

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

        currentPolyline = googleMap.addPolyline(new PolylineOptions()
                .add(new LatLng(40, 40), new LatLng(40, 40))
                .width(15)
                .color(Color.BLUE)
                .visible(false));

        oldPolyline = googleMap.addPolyline(new PolylineOptions()
                .add(new LatLng(40, 40), new LatLng(40, 40))
                .width(15)
                .color(Color.RED)
                .visible(false));

        oldPoints = new ArrayList<>();
        currentPoints = new ArrayList<>();

        healthHelperPolyPoints = new HealthHelperPolyPoints();
        healthHelperPolyPoints.setChangeListener(new OnLatLngChangeListener() {
            @Override
            public void onLatLngChangeListener(LatLng point) {
                oldPoints.add(point);

                if(oldPoints.size() > 1) {
                    oldPolyline.setVisible(true);
                    oldPolyline.setPoints(oldPoints);

                    mapView.invalidate();
                }
            }
        });

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("polylineCoords")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                healthHelperPolyPoints.setPoint(new LatLng((double) document.get("latitude"), (double) document.get("longitude")));
                                document.getReference().delete();
                            }
                        } else {
                            System.out.println("Failed to fetch data!");
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if(!mapPositionInit) {
            CameraPosition.Builder camBuilder = CameraPosition.builder();
            camBuilder.target(lastLocation);
            camBuilder.zoom(20);

            CameraPosition cp = camBuilder.build();

            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            mapPositionInit = true;
        }

        HashMap<String, Object> coords = new HashMap<>();
        coords.put("latitude", this.lastLocation.latitude);
        coords.put("longitude", this.lastLocation.longitude);

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("polylineCoords")
                .document()
                .set(coords);

        currentPoints.add(this.lastLocation);

        if(currentPoints.size() > 1) {
            currentPolyline.setVisible(true);
            currentPolyline.setPoints(currentPoints);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private File createImageFile() throws IOException {
        String fileName = Long.toString(System.currentTimeMillis());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File img = File.createTempFile(fileName,".jpg", dir);
        Log.i("Filename", fileName);
        Log.i("dir", dir.getPath());
        Log.i("img", img.getPath());

        photoPath = img.getAbsolutePath();

        gmap.addMarker(new MarkerOptions().position(new LatLng(lastLocation.latitude, lastLocation.longitude)));

        HashMap<String, Object> markerInfo = new HashMap<>();
        markerInfo.put("markerID",System.currentTimeMillis());
        markerInfo.put("latitude", this.lastLocation.latitude);
        markerInfo.put("longitude", this.lastLocation.longitude);
        markerInfo.put("imagePath", this.photoPath);

        FirebaseFirestore.getInstance()
                .collection("HealthHelper")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("markerInfo")
                .document()
                .set(markerInfo);

        Log.i("img", photoPath);

        return img;
    }
}
