package pt.ua.aguiar.sergio.healthhelper.Extras.Structures;

import android.graphics.Color;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnTimesChangeListener;

public class HealthHelperTimes {

    private OnTimesChangeListener changeListener;
    private int moving;
    private int stopped;

    public HealthHelperTimes() {
        this.changeListener = null;
        this.moving = 0;
        this.stopped = 0;
    }

    public void setChangeListener(OnTimesChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public void incrementMoving() {
        this.moving++;
        this.changeListener.onTimesChangeListener(this.moving, this.stopped);
    }

    public void incrementStopped() {
        this.stopped++;
        this.changeListener.onTimesChangeListener(this.moving, this.stopped);
    }

    public int getMoving() {
        return moving;
    }

    public int getStopped() {
        return stopped;
    }

    public static double calcBarPosition(int moving, int stopped) {
        return ((double) stopped) /((double) moving + (double) stopped) * 100;
    }

    public static String calcBarText(double barPosition) {
        return (barPosition <= 33.3) ? "Good" : (barPosition >= 66.6) ? "Bad" : "Average";
    }

    public static int calcTextColor(double barPosition) {
        return (barPosition <= 33.3) ? Color.BLUE : (barPosition >= 66.6) ? Color.RED : Color.MAGENTA;
    }

    public static String fromIntTimeToStringTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int secondsLeft = totalSeconds - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String output = hours + ":";
        if(minutes < 10) output += 0;
        output += minutes + ":";
        if(seconds < 10) output += 0;
        return output + seconds;
    }
}
