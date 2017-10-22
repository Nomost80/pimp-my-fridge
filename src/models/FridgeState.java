package models;

public class FridgeState {

    private int temperature;
    private int dampness;

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getDampness() {
        return dampness;
    }

    public void setDampness(int dampness) {
        this.dampness = dampness;
    }

    @Override
    public String toString() {
        return "temperature : " + this.temperature + " - dampness : " + this.dampness;
    }
}
