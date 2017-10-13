package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.postgresql.ds.PGPoolingDataSource;

/**
 * Common DFU interface. Not used for now as it would likely complicate code analysis and repair of parameterized queries.
 * 
 * @author psamouel
 *
 */
public interface DataDFU {

	CachedRowSet execute(PGPoolingDataSource dataSource, Map<String, Object> parameters) throws Exception;
}
