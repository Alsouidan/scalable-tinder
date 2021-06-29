package Database;

import Config.Config;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgreSQL {
    private static final Logger LOGGER = Logger
            .getLogger(PostgreSQL.class.getName());
    Config conf = Config.getInstance();
    private String DB_USERNAME = conf.getPostgresqlUserName();  //your db username
    private String DB_PASSWORD = conf.getPostgresqlPassword(); //your db password
    private String DB_PORT = conf.getPostgresqlPort();
    private String DB_HOST = conf.getPostgresqlHost();
    private String DB_NAME = conf.getPostgresqlDBName();
    private String DB_URL;
    private String DB_INIT_CONNECTIONS = conf.getPostgresqlInitConn();
    private String DB_MAX_CONNECTIONS = conf.getPostgresqlMaxConn();
    private PoolingDriver dbDriver;
    private Boolean isPopulate = conf.getPostgresqlPopulate();
    private PoolingDataSource<PoolableConnection> dataSource;
    private PoolableConnectionFactory poolableConnectionFactory;

    public static void disconnect(ResultSet rs, PreparedStatement statement,
                                  Connection conn, Statement query) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }

        if (query != null) {
            try {
                query.close();
            } catch (SQLException e) {
                e.printStackTrace();LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }

    public static void disconnect(ResultSet rs, PreparedStatement statement,
                                  Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }


    }

    public void shutdownDriver() throws SQLException {
        dbDriver.closePool(DB_NAME);
    }

    public void printDriverStats() throws SQLException {
        ObjectPool<? extends Connection> connectionPool = dbDriver
                .getConnectionPool(DB_NAME);

        System.out.println("DB Active Connections: "
                + connectionPool.getNumActive());
        System.out.println("DB Idle Connections: "
                + connectionPool.getNumIdle());
    }

    public PoolingDataSource<PoolableConnection> getDataSource() {
        return dataSource;
    }

    public void initSource() {
        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE,
                        "Error loading Postgres driver: " + ex.getMessage(), ex);
            }

            try {


                DB_URL = "jdbc:postgresql://" + DB_HOST + ':' + DB_PORT + "/" + DB_NAME;

            } catch (Exception e1) {
                try {
//                    readConfFile();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                System.out.println("Used Config File For DB");
            }


            Properties props = new Properties();
            //  System.out.println(DB_USERNAME);
            props.setProperty("user", DB_USERNAME);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("initialSize", DB_INIT_CONNECTIONS);
            props.setProperty("maxActive", DB_MAX_CONNECTIONS);

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    DB_URL, props);
            poolableConnectionFactory = new PoolableConnectionFactory(
                    connectionFactory, null);
            poolableConnectionFactory.setPoolStatements(true);

            setConnection();
            System.out.println("Connected to PostGresql");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Got error initializing data source: "
                    + ex.getMessage(), ex);
        }
    }
    private void setConnection() throws ClassNotFoundException, SQLException {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(Integer.parseInt(DB_INIT_CONNECTIONS));
        poolConfig.setMaxTotal(Integer.parseInt(DB_MAX_CONNECTIONS));
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(
                poolableConnectionFactory, poolConfig);
        poolableConnectionFactory.setPool(connectionPool);

        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        dbDriver = (PoolingDriver) DriverManager
                .getDriver("jdbc:apache:commons:dbcp:");
        dbDriver.registerPool(DB_NAME, connectionPool);

        dataSource = new PoolingDataSource<>(connectionPool);
    }
   

    public void setDBUser(String name) {
        DB_USERNAME = name;
    }

    public void setDBPassword(String pass) {
        DB_PASSWORD = pass;
    }

    public void setDBPort(String port) {
        DB_PORT = port;
    }

    public void setDBHost(String host) {
        DB_HOST = host;
    }


    public void setDBName(String name) {
        DB_NAME = name;
    }

    public void setDbInitConnections (String initConnections){
        DB_INIT_CONNECTIONS = initConnections;
        try {
            setConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.log(Level.SEVERE,throwables.getMessage(),throwables);

        }
    }

    public boolean setDbMaxConnections (String maxConnections ){
        DB_MAX_CONNECTIONS =maxConnections;
        try {
            this.setConnection();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.log(Level.SEVERE,throwables.getMessage(),throwables);
            return false;

        }
    }

    public String getDbMaxConnections() {
        return DB_MAX_CONNECTIONS;
    }

 
//    private boolean formatURL() {
//        setDBURL("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
//        System.out.println("database url..."+DB_URL);
//        Pattern pattern = Pattern.compile("^\\w+:\\w+:\\/{2}\\d+.\\d+.\\d+.\\d+:\\d+\\/\\w+(?:\\W|\\w)*$");
//        Matcher matcher = pattern.matcher(DB_URL);
//        return matcher.matches();
//    }

    Connection dbConn;
    public void populateDB(){

        try {
           initSource();
           dbConn = this.getDataSource().getConnection();
           dbConn.setAutoCommit(true);
           Statement query = dbConn.createStatement();
           query.setPoolable(true);
           query.executeUpdate(SqlScripts.dropScript);
           query.executeUpdate(SqlScripts.createTablesScript);
           query.executeUpdate(SqlScripts.inserStionScript);

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
           this.disconnect(null, null,dbConn);
        }
    }


}