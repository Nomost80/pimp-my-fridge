package models.db;

import java.sql.SQLException;

public class Tests_BDD {
    public static void main(String[] args)
    {
        DB_ValuesSensors test = null;
        System.out.println("Main pour tests BDD");
        try {
            test = new DB_ValuesSensors();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (test != null)
        {
            TimeSeries_AWT.content(test);
        }
    }
}
