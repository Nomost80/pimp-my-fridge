package models.db;

import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.sql.*;

public class DB_ValuesSensors extends DBEntity<Value>{

    private static abstract class QueryDB {
        public static String insertValues(){
            return "{CALL insert_Values(?, ?, ?)}" ;
        }
        public static String selectValuesFromSensor(){
            return "{CALL select_ValuesFromSensor(?, ?, ?)}" ;
        }
        public static String selectSensorsDescriptions(){
            return "{CALL select_SensorDescription(?)}" ;
        }
    }

    /**
     * Instantiates a new DAO entity.
     *
     * @param connection the connection
     * @throws SQLException the SQL exception
     */
    public DB_ValuesSensors(Connection connection) throws SQLException
    {
        super(connection);
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

    boolean insertAllValues()
    {
    //    insertOneValue();
        return false;
    }

    private boolean insertOneValue(Date times, String sensor, float value)
    {
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.insertValues());
            call.setDate(1, times);
            call.setString(2, sensor);
            call.setFloat(3, value);
            call.execute();
            return true ;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String recup_SensorDescription(String sensorName){
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.selectSensorsDescriptions());
            call.setString(1, sensorName);
            call.execute();
            final ResultSet resultSet = call.getResultSet();
            if (resultSet.next()) {
                return resultSet.getString("Description");
            }
        } catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public TimeSeriesCollection select_Series(String dateStart, String dateEnd){
        TimeSeriesCollection col = new TimeSeriesCollection();
        col.addSeries(select_Serie("Capteur 1", dateStart, dateEnd));
        col.addSeries(select_Serie("Capteur 2", dateStart, dateEnd));
        col.addSeries(select_Serie("Capteur 3", dateStart, dateEnd));
        col.addSeries(select_Serie("Capteur 4", dateStart, dateEnd));
        return col;
    }

    private TimeSeries select_Serie(String sensor, String start, String end){
        String title = recup_SensorDescription(sensor);
        return recup_SensorValues(sensor, title, start, end);
    }

    private TimeSeries recup_SensorValues(String sensorName, String title, String dateStart, String dateEnd){
        final TimeSeries series = new TimeSeries(title);
        try {
            final CallableStatement call = this.getConnection().prepareCall(QueryDB.selectValuesFromSensor());
            call.setString(1, sensorName);
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
