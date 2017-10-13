package mil.darpa.immortals.dfus.TakServerDataManager;

import static org.junit.Assert.*;

import javax.sql.rowset.CachedRowSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mil.darpa.immortals.dfus.TakServerDataManager.DataManager;

public class DataManagerTest {

	private DataManager dm = null;

	@Before
	public void setUp() throws Exception {
		dm = new DataManager();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCotEventsForConstantChannelJoin() {

		try {
			CachedRowSet data = dm.cotEventsForConstantChannelJoin();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForConstantChannelJoin2() {

		try {
			CachedRowSet data = dm.cotEventsForConstantChannelJoin2();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForConstantCompoundFilter() {

		try {
			CachedRowSet data = dm.cotEventsForConstantCompoundFilter();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForConstantMixedJoin() {

		try {
			CachedRowSet data = dm.cotEventsForConstantMixedJoin();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForConstantTimeInterval() {

		try {
			CachedRowSet data = dm.cotEventsForConstantTimeInterval();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForUidAndInterval() {

		String sourceName = "ICAO-ACEA99";
		Long timeInterval = new Long(201705071640l);

		try {
			CachedRowSet data = dm.cotEventsForUidAndInterval(sourceName, timeInterval);
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsOnChannelInRegion() {

		try {
			CachedRowSet data = dm.cotEventsOnChannelInRegion();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

	@Test
	public void testCotEventsForConstantCotType() {

		try {
			CachedRowSet data = dm.cotEventsForConstantCotType();
			assertTrue(data.size() > 0);
		} catch (Exception e) {
			fail("Unexpected error: " + e);
		}
	}

}
