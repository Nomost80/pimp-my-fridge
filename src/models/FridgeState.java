package models;

import java.util.ArrayList;
import java.util.Date;

public class FridgeState {

    private Date measuredAt;
    private Float brink;
    private ArrayList<Measurement> measurements;

    public FridgeState() {
        this.measurements = new ArrayList<>();
    }

    public Date getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(Date measuredAt) {
        this.measuredAt = measuredAt;
    }

    public Float getBrink() {
        return brink;
    }

    public void setBrink(Float brink) {
        this.brink = brink;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(ArrayList<Measurement> measurements) {
        this.measurements = measurements;
    }
}
