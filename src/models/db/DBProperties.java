package models.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class DBProperties.
 *
 * @author Jean-Aymeric Diet
 */
class DBProperties extends Properties {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5289057445894568927L;

    /** The Constant PROPERTIES_FILE_NAME. */
    private final static String	PROPERTIES_FILE_NAME = "src\\models\\db\\model.properties";

    /** The working dir */
    private final static String WORKING_DIR = System.getProperty("user.dir");

    /** The url. */
    private String url = "";

    /** The login. */
    private String login = "";

    /** The password. */
    private String password = "";

    /**
     * Instantiates a new DB properties.
     */
    public DBProperties() {
        InputStream inputStream = null;
        File temp = new File(DBProperties.WORKING_DIR, DBProperties.PROPERTIES_FILE_NAME);
        //inputStream = this.getClass().getClassLoader().getResourceAsStream(DBProperties.PROPERTIES_FILE_NAME);
        try{
            inputStream = new FileInputStream(temp);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        if (inputStream != null) {
            try {
                this.load(inputStream);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            this.setUrl(this.getProperty("url"));
            this.setLogin(this.getProperty("login"));
            this.setPassword(this.getProperty("password"));
        }
    }

    public void test(){
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);

        File temp = new File(workingDir, "test.properties");

        String absolutePath = temp.getAbsolutePath();
        System.out.println("File path : " + absolutePath);

        try {
            Properties properties = new Properties();
            InputStream resourceAsStream =  new FileInputStream(temp);
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url.
     *
     * @param url
     *          the new url
     */
    private void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Sets the login.
     *
     * @param login
     *          the new login
     */
    private void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password.
     *
     * @param password
     *          the new password
     */
    private void setPassword(final String password) {
        this.password = password;
    }
}