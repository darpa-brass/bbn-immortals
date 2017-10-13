package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;

public class CotEventsForConstantCompoundFilter {

	public CachedRowSet execute(PGPoolingDataSource dataSource) throws Exception {

		String query = "select id, source_id, cot_type, how " + 
				"from cot_event " + 
				"where servertime = 201705071635 " + 
				"and cot_type = 'a-n-A-C-F-m'";
	
		CachedRowSet rowset = null;
		
		try (	Connection conn = dataSource.getConnection();
				ResultSet rs = conn.prepareStatement(query).executeQuery()) {
			rowset = RowSetProvider.newFactory().createCachedRowSet();
			rowset.populate(rs);
		}
		
		return rowset;

	}
}
