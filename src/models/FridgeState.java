package models;

import java.util.ArrayList;
import java.util.Date;

public class FridgeState {

    private Date measuredAt;
    private int brink;
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

    public int getBrink() {
        return brink;
    }

    public void setBrink(int brink) {
        this.brink = brink;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(ArrayList<Measurement> measurements) {
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        String str = String.format("measured_at: %s - brink: %d - measurements:", this.measuredAt, this.brink);
        for (Measurement measurement : this.measurements)
            str += String.format("sensor: %s - label: %s - value: %f", measurement.getSensor(), measurement.getLabel()
            , measurement.getValue());
        return str;
    }
}
