package models;

import java.util.regex.Pattern;

public class FridgeState {

    private static Pattern pattern = Pattern.compile("\\d+");
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

    public static Pattern getPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return "temperature : " + this.temperature + " - dampness : " + this.dampness;
    }
}
