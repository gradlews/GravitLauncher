package pro.gravit.launchserver.auth;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.VerifyHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class MySQLSourceConfig implements AutoCloseable {

    public static final int TIMEOUT = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("launcher.mysql.idleTimeout", Integer.toString(5000))),
            VerifyHelper.POSITIVE, "launcher.mysql.idleTimeout can't be <= 5000");
    private static final int MAX_POOL_SIZE = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("launcher.mysql.maxPoolSize", Integer.toString(3))),
            VerifyHelper.POSITIVE, "launcher.mysql.maxPoolSize can't be <= 0");

    // Instance
    private transient final String poolName;

    // Config
    private String address;
    private int port;
    private boolean useSSL;
    private boolean verifyCertificates;
    private String username;
    private String password;
    private String database;
    private String timeZone;
    private boolean enableHikari;

    // Cache
    private transient DataSource source;
    private transient boolean hikari;


    public MySQLSourceConfig(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public synchronized void close() {
        if (hikari)
            ((HikariDataSource) source).close();
    }


    public synchronized Connection getConnection() throws SQLException {
        if (source == null) { // New data source
            MysqlDataSource mysqlSource = new MysqlDataSource();
            mysqlSource.setCharacterEncoding("UTF-8");

            // Prep statements cache
            mysqlSource.setPrepStmtCacheSize(250);
            mysqlSource.setPrepStmtCacheSqlLimit(2048);
            mysqlSource.setCachePrepStmts(true);
            mysqlSource.setUseServerPrepStmts(true);

            // General optimizations
            mysqlSource.setCacheServerConfiguration(true);
            mysqlSource.setUseLocalSessionState(true);
            mysqlSource.setRewriteBatchedStatements(true);
            mysqlSource.setMaintainTimeStats(false);
            mysqlSource.setUseUnbufferedInput(false);
            mysqlSource.setUseReadAheadInput(false);
            mysqlSource.setUseSSL(useSSL);
            mysqlSource.setVerifyServerCertificate(verifyCertificates);
            // Set credentials
            mysqlSource.setServerName(address);
            mysqlSource.setPortNumber(port);
            mysqlSource.setUser(username);
            mysqlSource.setPassword(password);
            mysqlSource.setDatabaseName(database);
            mysqlSource.setTcpNoDelay(true);
            if (timeZone != null) mysqlSource.setServerTimezone(timeZone);
            hikari = false;
            // Try using HikariCP
            source = mysqlSource;
            if (enableHikari) {
                try {
                    Class.forName("com.zaxxer.hikari.HikariDataSource");
                    hikari = true; // Used for shutdown. Not instanceof because of possible classpath error
                    HikariConfig cfg = new HikariConfig();
                    cfg.setDataSource(mysqlSource);
                    cfg.setPoolName(poolName);
                    cfg.setMaximumPoolSize(MAX_POOL_SIZE);
                    // Set HikariCP pool
                    // Replace source with hds
                    source = new HikariDataSource(cfg);
                    LogHelper.warning("HikariCP pooling enabled for '%s'", poolName);
                } catch (ClassNotFoundException ignored) {
                    LogHelper.debug("HikariCP isn't in classpath for '%s'", poolName);
                }
            }

        }
        return source.getConnection();
    }
}
