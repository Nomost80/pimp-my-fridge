package models.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The Class DBConnection.
 *
 * @author Jean-Aymeric Diet
 */
final class DBConnection {
    /** The instance. */
    private static DBConnection	INSTANCE = null;

    /** The connection. */
    private Connection connection;

    /**
     * Instantiates a new DB connection.
     */
    private DBConnection() {
        this.open();
    }

    /**
     * Gets the single instance of DBConnection.
     *
     * @return single instance of DBConnection
     */
    public static synchronized DBConnection getInstance() {
        if (DBConnection.INSTANCE == null) {
            DBConnection.INSTANCE = new DBConnection();
        }
        return DBConnection.INSTANCE;
    }

    /**
     * Open.
     *
     * @return the boolean
     */
    private Boolean open() {
        final DBProperties dbProperties = new DBProperties();
        try {
            Class.forName("com.mysql.jdbc.Driver");
    //        System.out.println("URL : " + dbProperties.getUrl());
    //        System.out.println("getLogin : " + dbProperties.getLogin());
    //        System.out.println("getPassword : " + dbProperties.getPassword());
            this.connection = DriverManager.getConnection(dbProperties.getUrl(), dbProperties.getLogin(), dbProperties.getPassword());
            return true;
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("The connection was not established");
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return this.connection;
    }
}

