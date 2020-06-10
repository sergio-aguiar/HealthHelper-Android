package pt.ua.aguiar.sergio.healthhelper.Extras.Structures;

import com.google.android.gms.maps.model.LatLng;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnMarkerUpdateFlaggedListener;

public class HealthHelperMarker {

    private OnMarkerUpdateFlaggedListener changeListener;
    private final String markerInfo;
    private final LatLng locationInfo;
    private final String imagePath;

    public HealthHelperMarker(String markerInfo, LatLng locationInfo, String imagePath) {
        changeListener = null;
        this.markerInfo = markerInfo;
        this.locationInfo = locationInfo;
        this.imagePath = imagePath;
    }

    public String getMarkerInfo() {
        return this.markerInfo;
    }

    public Double getLatitude() {
        return this.locationInfo.latitude;
    }

    public Double getLongitude() {
        return this.locationInfo.longitude;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void flagUpdate() {
        this.changeListener.onMarkerCreatedListener(this);
    }

    public void setChangeListener(OnMarkerUpdateFlaggedListener changeListener) {
        this.changeListener = changeListener;
    }
}
