package models;

public class Measurement {

    private String sensor;
    private String label;
    private float value;

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("sensor: %s - label: %s - value: %f", this.sensor, this.label, this.value);
    }
}
