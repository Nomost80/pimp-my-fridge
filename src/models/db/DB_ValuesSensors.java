package models.db;

import models.FridgeState;
import models.Measurement;
import models.SerialPublisher;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.sql.*;
import java.util.Objects;

public class DB_ValuesSensors extends DBEntity<Value>{
    private final SerialPublisher serialPublisher;

    private static abstract class QueryDB {
        static String insertValues(){
            return "{CALL insert_Values(?, ?, ?, ?)}" ;
        }
        static String selectValuesFromSensor(){
            return "{CALL select_ValuesFromSensor(?, ?, ?, ?)}" ;
        }
    }

    /**
     * Instantiates a new DAO entity.
     *
     * @throws SQLException the SQL exception
     */
    public DB_ValuesSensors(SerialPublisher serialPublisher) throws SQLException
    {
        super(DBConnection.getInstance().getConnection());
        this.serialPublisher = serialPublisher;
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
            insertOneValue(date, "Brink", "Brink Temperature", fridgeState.getBrink());
            insertOneValue(date, "Dew", "Dew Point", (float) this.serialPublisher.pntRosee_Value(fridgeState));
        }
    }

    private boolean insertOneValue(Timestamp times, String sensor, String description, Float value)
    {
        if (value != -1)
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
        }
        return false;
    }

    public TimeSeriesCollection select_TemperaturesSeries(FridgeState fridgeState, String dateStart, String dateEnd){
        TimeSeriesCollection col = new TimeSeriesCollection();
        for (Measurement measurement : fridgeState.getMeasurements())
        {
            if (!Objects.equals(measurement.getLabel(), "Dampness"))
            {
                col.addSeries(select_Serie(measurement.getSensor(), measurement.getLabel(), dateStart, dateEnd));
            }
        }
        col.addSeries(select_Serie("Brink", "Brink Temperature", dateStart, dateEnd));
        col.addSeries(select_Serie("Dew", "Dew Point", dateStart, dateEnd));
        return col;
    }

    public TimeSeriesCollection select_DampnessSerie(FridgeState fridgeState, String dateStart, String dateEnd){
        TimeSeriesCollection col = new TimeSeriesCollection();
        for (Measurement measurement : fridgeState.getMeasurements())
        {
            if (Objects.equals(measurement.getLabel(), "Dampness"))
            {
                col.addSeries(select_Serie(measurement.getSensor(), measurement.getLabel(), dateStart, dateEnd));
            }
        }
        return col;
    }

    private TimeSeries select_Serie(String sensorName, String description, String dateStart, String dateEnd){
        final TimeSeries series = new TimeSeries(description);
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.selectValuesFromSensor());
            call.setString(1, sensorName);
            call.setString(2, description);
            call.setString(3, dateStart);
            call.setString(4, dateEnd);
            call.execute();
            final ResultSet resultSet = call.getResultSet();
            while (resultSet.next()) {
        //        System.out.println("Date reçue : " + resultSet.getTimestamp("Times"));
        //        System.out.println("Second : " + new Second(resultSet.getTimestamp("Times")));
                Second times = new Second(resultSet.getTimestamp("Times"));
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
