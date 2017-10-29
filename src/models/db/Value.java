package models.db;

import java.sql.Date;

public class Value extends Entity {
    private Date times;
    private String sensor;
    private Float value;

    public Value(Date times, String sensor, Float value)
    {
        this.times = times;
        this.sensor = sensor;
        this.value = value;
    }

    public Date getTimes() {
        return times;
    }

    public Float getValue() {
        return value;
    }

    public String getSensor() {
        return sensor;
    }
}
