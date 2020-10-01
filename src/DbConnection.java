import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

public class DbConnection {
	// Location/name of the .ini file.
	private static final String INI_FILE = "inifile.ini";
	// The max number of database connection retries.
	private static final int MAX_RETRY = 3;
	// Max size of connection pool.
	private static final int POOL_SIZE = 12;
	
	// List of used and unused connection pools.
	private static List<Connection> connectionPool, usedConnections = new ArrayList<>();
	
	// Initialize database configuration variables.
	private static String hostName, port, databaseName, username, password;
	
	/**
	 * Creates a connection pool.
	 */
	public static void createConnectionPool() {
		// Initialize the pool
		connectionPool = new ArrayList<>(POOL_SIZE);
		
		for (int i = 0;i < POOL_SIZE;i++) {
			connectionPool.add(db_connect());
		}
	}
	
	/**
	 * Gets a connection from the connection pool.
	 * @return a connection from the connection pool.
	 */
	public static Connection get_connection() {
		// Check if connection pool is empty
		if (connectionPool.isEmpty()) {
			// Create a connection pool if number of pools is less than max pool size.
			if (usedConnections.size() < POOL_SIZE) {
				connectionPool.add(db_connect());
			}
			// All connections are being used.
			else {
				JOptionPane.showMessageDialog(null, "No available connections from pool.");
				return null;
			}
		}
		
		// Get one connection from the connection pool.
		Connection conn = connectionPool.remove(connectionPool.size() - 1);
		
		// Check if connection is still alive and working.
		try {
			if (!conn.isValid(1000)) {
				conn = db_connect();
			}
		}
		// Error establishing connection with database.
		catch(Exception e) {
			conn = null;
		}
		
		// Set the connection as used connection.
		usedConnections.add(conn);
		
		return conn;
	}
	
	/**
	 * Releases a connection and add it back to the connection pool.
	 * @param conn is the connection to be released.
	 */
	public static void releaseConnection(Connection conn) {
		connectionPool.add(conn);
		usedConnections.remove(conn);
	}
	
	/**
	 * Returns the database connection.
	 * @return a connection with a database.
	 */
	public static Connection db_connect() {
		Connection conn = null;
		
		// Try to establish database connection.
		try {
			// Initialize current number of retries.
			int retryCount = 0;
			
			// Repeat if no connection until max attempt is reached.
			while (conn == null && retryCount < MAX_RETRY) {
				// Get connection.
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(
						"jdbc:mysql://" + hostName + ":" + port + "/" + databaseName
						+ "?useLegacyDatetimeCode=false&serverTimezone=Asia/Singapore",
						username, password);
				
				// If no connection is established, increment number of retries and wait for
				// a fixed amount of time
				if (!conn.isValid(1000)) {
					System.out.println("Error connecting to database. Retrying...");
					retryCount++;
				}
			}
		}
		// Error getting connection.
		catch (Exception e) {
			// Empty
		}
		
		return conn;
	}
	
	/**
	 * Initializes required configuration values.
	 */
	public static void initialize_db() {
		// Get the properties of .ini file.
		Properties properties = load_ini();
		
		// Extract the properties of .ini file.
		hostName = properties.getProperty("hostname");
		port = properties.getProperty("port");
		databaseName = properties.getProperty("databasename");
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		
		// Create the connection pool after loading configuration.
		createConnectionPool();
	}
	
	/**
	 * Closes Statements and ResultSet.
	 * @param stm is the Statement to be closed.
	 * @param ps is the PreparedStatement to be closed.
	 * @param rs is the ResultSet to be closed.
	 */
	public static void close(Statement stm, PreparedStatement ps, ResultSet rs) {
		if (stm != null) {
	        try {
	            stm.close();
	        } 
	        catch (SQLException e) {
	        	// Empty
	        }
		}
		if (ps != null) {
	        try {
	            ps.close();
	        } 
	        catch (SQLException e) {
	        	// Empty
	        }
		}
		if (rs != null) {
	        try {
	            rs.close();
	        } 
	        catch (SQLException e) {
	        	// Empty
	        }
		}
	}
	
	/**
	 * Returns the properties of .ini file.
	 * @return the properties of loaded .ini file.
	 */
	private static Properties load_ini() {
		Properties p = new Properties();
		
		// Try to load file.
		try {
			p.load(new FileInputStream(INI_FILE));
		}
		// Error loading the file.
		catch (Exception e) {
			System.out.println("Error loading inifile.ini");
		}
		
		return p;
	}
}
