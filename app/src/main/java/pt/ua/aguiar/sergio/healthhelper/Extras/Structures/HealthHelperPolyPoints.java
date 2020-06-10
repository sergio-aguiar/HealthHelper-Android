package pt.ua.aguiar.sergio.healthhelper.Extras.Structures;

import com.google.android.gms.maps.model.LatLng;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnLatLngChangeListener;

public class HealthHelperPolyPoints {

    private OnLatLngChangeListener changeListener;
    private LatLng point;

    public HealthHelperPolyPoints() {
        changeListener = null;
        point = null;
    }

    public void setChangeListener(OnLatLngChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void setPoint(LatLng point) {
        this.point = point;
        this.changeListener.onLatLngChangeListener(this.point);
    }
}
