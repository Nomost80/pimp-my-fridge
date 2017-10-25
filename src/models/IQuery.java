package models;

import org.jfree.data.time.TimeSeriesCollection;

public interface IQuery {
    TimeSeriesCollection select_TemperaturesSeries(FridgeState fridgeState, String dateStart, String dateEnd);
    TimeSeriesCollection select_DampnessSerie(FridgeState fridgeState, String dateStart, String dateEnd);
    double pntRosee_Value(FridgeState fridgeState);
    Enum_AlarmStates pntRosee_Alarm(FridgeState fridgeState);
}
