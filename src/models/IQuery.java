package models;

import org.jfree.data.time.TimeSeriesCollection;

public interface IQuery {
    TimeSeriesCollection select_TemperaturesSeries(FridgeState fridgeState, String dateStart, String dateEnd);
    TimeSeriesCollection select_DampnessSerie(FridgeState fridgeState, String dateStart, String dateEnd);
    boolean ckeck_PntRosee();
}
