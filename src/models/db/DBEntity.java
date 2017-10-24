package models.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Class DAOEntity.
 *
 * @author Samuel Ducange
 *
 * @param <E>
 *          the element type
 */
abstract class DBEntity<E extends Entity> {

    /** The connection. */
    private final Connection connection;

    /**
     * Instantiates a new DAO entity.
     *
     * @param connection
     *          the connection
     * @throws SQLException
     *           the SQL exception
     */
    public DBEntity(final Connection connection) throws SQLException {
        this.connection = connection;
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     */
    protected Connection getConnection() {
        return this.connection;
    }

    /**
     * Creates the.
     *
     * @return true, if successful
     */
    public abstract boolean create();

    /**
     * Delete.
     *
     * @param entity
     *          the entity
     * @return true, if successful
     */
    public abstract boolean delete(E entity);

    /**
     * Update.
     *
     * @param entity
     *          the entity
     * @return true, if successful
     */
    public abstract boolean update(E entity);

    /**
     * Find.
     *
     * @param id
     *          the id
     * @return the e
     */
    public abstract E find(int id);
}

