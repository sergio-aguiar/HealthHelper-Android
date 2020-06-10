package pt.ua.aguiar.sergio.healthhelper.Extras.Structures;

import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnListenedResultChangeListener;

public class HealthHelperListenedResult {

    private OnListenedResultChangeListener changeListener;
    private boolean resultObtained;
    private boolean result;

    public HealthHelperListenedResult() {
        this.changeListener = null;
        this.resultObtained = false;
        this.result = false;
    }

    public void setResult(boolean result) {
        this.resultObtained = true;
        this.result = result;
        if(this.changeListener != null) this.changeListener.onListenedResultChangeListener(this);
    }

    public boolean getResult() {
        return this.result;
    }

    public boolean isResultObtained() {
        return this.resultObtained;
    }

    public void setChangeListener(OnListenedResultChangeListener changeListener) {
        this.changeListener = changeListener;
    }
}
