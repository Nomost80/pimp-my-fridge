package models.db;

import models.FridgeState;
import models.Measurement;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.sql.*;

public class DB_ValuesSensors extends DBEntity<Value>{

    private static abstract class QueryDB {
        public static String insertValues(){
            return "{CALL insert_Values(?, ?, ?, ?)}" ;
        }
        public static String selectValuesFromSensor(){
            return "{CALL select_ValuesFromSensor(?, ?, ?, ?)}" ;
        }
        public static String selectSensorsDescriptions(){
            return "{CALL select_SensorDescription(?)}" ;
        }
    }

    /**
     * Instantiates a new DAO entity.
     *
     * @throws SQLException the SQL exception
     */
    public DB_ValuesSensors() throws SQLException
    {
        super(DBConnection.getInstance().getConnection());
    }

    @Override
    public boolean create()
    {
        return false;
    }

    @Override
    public boolean delete(final Value entity)
    {
        return false;
    }

    @Override
    public boolean update(final Value entity)
    {
        return false;
    }

    @Override
    public Value find(final int id)
    {
        return null;
    }

    public void insertAllValues(FridgeState fridgeState)
    {
        if (fridgeState != null)
        {
            Timestamp date = new Timestamp(new java.util.Date().getTime());
            for (Measurement measurement : fridgeState.getMeasurements())
            {
                insertOneValue(date, measurement.getSensor(), measurement.getLabel(), measurement.getValue());
            }
        }
    }

    private boolean insertOneValue(Timestamp times, String sensor, String description, float value)
    {
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.insertValues());
            call.setTimestamp(1, times);
            call.setString(2, sensor);
            call.setString(3, description);
            call.setFloat(4, value);
            call.execute();
            return true ;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public TimeSeriesCollection select_Series(FridgeState fridgeState, String dateStart, String dateEnd){
        TimeSeriesCollection col = new TimeSeriesCollection();
        for (Measurement measurement : fridgeState.getMeasurements())
        {
            col.addSeries(select_Serie(measurement.getSensor(), measurement.getLabel(), dateStart, dateEnd));
        }
        return col;
    }

    private TimeSeries select_Serie(String sensorName, String description, String dateStart, String dateEnd){
        final TimeSeries series = new TimeSeries(description);
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.selectValuesFromSensor());
            call.setString(1, sensorName);
            call.setString(1, description);
            call.setString(2, dateStart);
            call.setString(3, dateEnd);
            call.execute();
            final ResultSet resultSet = call.getResultSet();
            while (resultSet.next()) {
        //        System.out.println("Date reçue : " + resultSet.getTimestamp("Times"));
        //        System.out.println("Second : " + new Second(resultSet.getTimestamp("Times")));
                Second times = new Second(resultSet.getTime("Times"));
                Float value = resultSet.getFloat("Val");
                TimeSeriesDataItem item = new TimeSeriesDataItem(times, value);
                series.add(item);
            }
        } catch (final SQLException e)
        {
            e.printStackTrace();
        } catch (final SeriesException e)
        {
            System.err.println("Error adding to series : ");
            e.printStackTrace();
        }
        return series;
    }
}
