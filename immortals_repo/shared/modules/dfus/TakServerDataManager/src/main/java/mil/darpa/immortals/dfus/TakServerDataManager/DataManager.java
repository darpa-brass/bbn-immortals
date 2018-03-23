package mil.darpa.immortals.dfus.TakServerDataManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.rowset.CachedRowSet;

import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.datatypes.cot.CotHelper;
import mil.darpa.immortals.datatypes.cot.Event;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantChannelJoin;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantChannelJoin2;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantCompoundFilter;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantCotType;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantMixedJoin;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForConstantTimeInterval;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsForUidAndInterval;
import mil.darpa.immortals.dfus.TakServerDataManager.DFU.CotEventsOnChannelInRegion;

public class DataManager {

	private static PGPoolingDataSource dataSource;
	private final Logger logger = LoggerFactory.getLogger(DataManager.class);
	
	//We can move these config items to a config management solution
	private static final String COT_DATA_SOURCE = "TakDataSource";
	private static final String SERVER_NAME = "localhost";
	private static final String DATABASE_NAME = "immortals";
	private static final String USER = "immortals";
	private static final String PASSWORD = "immortals";
	private static final int MAXIMUM_NUMBER_CONNECTIONS = 4;
	private static final String REPORTING_SCHEMA = "takrpt";

	private static final String insertCotEvent = "INSERT INTO tak.COT_EVENT(source_id, cot_type, how, detail) " + 
			"SELECT id, ?, ?, ? from tak.source where name = ?";
	private static final String insertCotEventPosition = "INSERT INTO tak.COT_EVENT_POSITION (cot_event_id, point_hae, point_ce, point_le, longitude, latitude) VALUES (?,?,?,?,?,?)";
	private static final String DEFAULT_DETAIL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><detail/>";

	static {
		//Not the ideal pooling implementation, but should be fine for this project
		dataSource = new PGPoolingDataSource();
		dataSource.setDataSourceName(COT_DATA_SOURCE);
		dataSource.setServerName(SERVER_NAME);
		dataSource.setDatabaseName(DATABASE_NAME);
		dataSource.setUser(USER);
		dataSource.setPassword(PASSWORD);
		dataSource.setMaxConnections(MAXIMUM_NUMBER_CONNECTIONS);
		dataSource.setCurrentSchema(REPORTING_SCHEMA);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (dataSource != null) {
					dataSource.close();
				}
			}
		});
	}
	
	public DataManager() {}

	public void insertEvent(Event event) throws Exception {

		boolean success = false;
		
		Connection conn = null;
		PreparedStatement stmtCotEvent = null;
		PreparedStatement stmtCotEventPosition = null;
		String detailAsXML = null;
		
		//Validate essential elements of event
		if (event == null || event.getPoint() == null) {
			throw new Exception("Missing event or event.point in call to insertEvent.");
		}
		
		if (event.getPoint().getLon() == null || event.getPoint().getLat() == null) {
			throw new Exception("Missing lat/long values in call to insertEvent.");
		}
		
		if (event.getUid() == null || event.getUid().trim().length() == 0) {
			throw new Exception("Missing event.uid in call to insertEvent.");
		}
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			stmtCotEvent = conn.prepareStatement(insertCotEvent, Statement.RETURN_GENERATED_KEYS);
			stmtCotEventPosition = conn.prepareStatement(insertCotEventPosition);
	
			stmtCotEvent.setString(1, Utilities.nullStringTo(event.getType(), "null"));
			stmtCotEvent.setString(2, Utilities.nullStringTo(event.getHow(), "null"));
			if (event.getDetail() != null) {
				try {
					detailAsXML = CotHelper.marshalObject(event.getDetail());
				} catch (Exception e) {
					detailAsXML = DEFAULT_DETAIL_XML;
				}
				stmtCotEvent.setString(3, detailAsXML);
			} else {
				stmtCotEvent.setString(3, DEFAULT_DETAIL_XML);
			}
			stmtCotEvent.setString(4, event.getUid());
			
			int rowCount = stmtCotEvent.executeUpdate();
	
			if (rowCount == 1) {
				int generatedCotEventID = -1;
				try (ResultSet keys = stmtCotEvent.getGeneratedKeys()) {
					if (keys.next()) {
						generatedCotEventID = keys.getInt(1);
					}
				}

				if (generatedCotEventID > 0) {
					rowCount = 0;
					stmtCotEventPosition.setInt(1, generatedCotEventID);
					stmtCotEventPosition.setInt(2, Math.round(Utilities.nullBigDecimalToFloat(event.getPoint().getHae(), 9999999)));
					stmtCotEventPosition.setInt(3, Math.round(Utilities.nullBigDecimalToFloat(event.getPoint().getCe(), 9999999)));
					stmtCotEventPosition.setInt(4, Math.round(Utilities.nullBigDecimalToFloat(event.getPoint().getLe(), 9999999)));
					stmtCotEventPosition.setDouble(5, event.getPoint().getLon().doubleValue());
					stmtCotEventPosition.setDouble(6, event.getPoint().getLat().doubleValue());
					
					rowCount = stmtCotEventPosition.executeUpdate();
					if (rowCount == 1) {
						success = true;
					}
				}
			}

			if (success) {
				conn.commit();
				logger.debug("CoT Event persisted to database.");
			} else {
				conn.rollback();
				logger.error("Could not persist CotEvent to database.");
			}
		} catch (Exception e) {
			if (conn != null) {
				conn.rollback();
			}
		} finally {
			if (stmtCotEvent != null) {
				stmtCotEvent.close();
			}

			if (stmtCotEventPosition != null) {
				stmtCotEventPosition.close();
			}

			if (conn != null) {
				conn.close();
			}
		}

	}

	public CachedRowSet cotEventsForConstantCotType() throws SQLException {

		CotEventsForConstantCotType dfu = new CotEventsForConstantCotType();
		CachedRowSet rowset = null;

		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantCotType.");
		} catch (Exception e) {
			logger.error("Unable to complete getCotEventsForConstantCotType: " + e);
			rowset = null;
		}
		
		return rowset;

	}

	public CachedRowSet cotEventsForConstantTimeInterval() throws SQLException {

		CotEventsForConstantTimeInterval dfu = new CotEventsForConstantTimeInterval();
		CachedRowSet rowset = null;

		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantTimeInterval.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForConstantTimeInterval: " + e);
			rowset = null;
		}
		
		return rowset;
	}

	public CachedRowSet cotEventsForConstantCompoundFilter() throws SQLException {

		CotEventsForConstantCompoundFilter dfu = new CotEventsForConstantCompoundFilter();
		CachedRowSet rowset = null;
	
		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantCompoundFilter.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForConstantCompoundFilter: " + e);
			rowset = null;
		}
		
		return rowset;
	}

	public CachedRowSet cotEventsForConstantChannelJoin() throws SQLException {

		CotEventsForConstantChannelJoin dfu = new CotEventsForConstantChannelJoin();
		CachedRowSet rowset = null;
	
		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantChannelJoin.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForConstantChannelJoin: " + e);
			rowset = null;
		}
		
		return rowset;
	}

	public CachedRowSet cotEventsForConstantChannelJoin2() throws SQLException {

		CotEventsForConstantChannelJoin2 dfu = new CotEventsForConstantChannelJoin2();
		CachedRowSet rowset = null;
	
		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantChannelJoin2.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForConstantChannelJoin2: " + e);
			rowset = null;
		}
		
		return rowset;
	}

	public CachedRowSet cotEventsForConstantMixedJoin() throws SQLException {

		CotEventsForConstantMixedJoin dfu = new CotEventsForConstantMixedJoin();
		CachedRowSet rowset = null;
	
		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsForConstantMixedJoin.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForConstantMixedJoin: " + e);
			rowset = null;
		}
		
		return rowset;
	}
	
	public CachedRowSet cotEventsOnChannelInRegion() throws SQLException {

		CotEventsOnChannelInRegion dfu = new CotEventsOnChannelInRegion();
		CachedRowSet rowset = null;
	
		try {
			rowset = dfu.execute(dataSource);
			logger.debug("Successfully completed cotEventsOnChannelInRegion.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsOnChannelInRegion: " + e);
			rowset = null;
		}
		
		return rowset;
	}
    
	public CachedRowSet cotEventsForUidAndInterval(String sourceName, Long timeInterval) throws Exception {

		CotEventsForUidAndInterval dfu = new CotEventsForUidAndInterval();
		CachedRowSet rowset = null;
		
		try {
			rowset = dfu.execute(dataSource, sourceName, timeInterval);
			logger.debug("Successfully completed cotEventsForUidAndInterval.");
		} catch (Exception e) {
			logger.error("Unable to complete cotEventsForUidAndInterval: " + e);
			rowset = null;
		}
		
		return rowset;
	}

	public static void main(String[] args) throws Exception {
		
		DataManager dm = new DataManager();
		
		System.out.println("Running cotEventsForConstantChannelJoin");
		dm.cotEventsForConstantChannelJoin();

		System.out.println("Running cotEventsForConstantChannelJoin2");
		dm.cotEventsForConstantChannelJoin2();

		System.out.println("Running cotEventsForConstantCompoundFilter");
		dm.cotEventsForConstantCompoundFilter();

		System.out.println("Running cotEventsForConstantMixedJoin");
		dm.cotEventsForConstantMixedJoin();

		System.out.println("Running cotEventsForConstantTimeInterval");
		dm.cotEventsForConstantTimeInterval();

		System.out.println("Running cotEventsForUidAndInterval");
		dm.cotEventsForUidAndInterval("ICAO-ACEA99", new Long(201705071640l));

		System.out.println("Running cotEventsOnChannelInRegion");
		dm.cotEventsOnChannelInRegion();

		System.out.println("Running cotEventsForConstantCotType");
		dm.cotEventsForConstantCotType();

	}
	
}
