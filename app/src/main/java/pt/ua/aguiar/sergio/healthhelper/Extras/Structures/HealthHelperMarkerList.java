package pt.ua.aguiar.sergio.healthhelper.Extras.Structures;

import java.util.ArrayList;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnMarkerListChangeListener;

public class HealthHelperMarkerList {

    private OnMarkerListChangeListener changeListener;
    private ArrayList<HealthHelperMarker> healthHelperMarkers;

    public HealthHelperMarkerList() {
        this.changeListener = null;
        this.healthHelperMarkers = new ArrayList<>();
    }

    public void addMarker(HealthHelperMarker marker) {
        this.healthHelperMarkers.add(marker);
        if(this.changeListener != null) this.changeListener.onMarkerListChangeListener(this.healthHelperMarkers);
    }

    public void setOnMarkerListChangeListener(OnMarkerListChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public ArrayList<HealthHelperMarker> getHealthHelperMarkers() {
        return healthHelperMarkers;
    }

    @Override
    public String toString() {
        return this.healthHelperMarkers.toString();
    }
}
