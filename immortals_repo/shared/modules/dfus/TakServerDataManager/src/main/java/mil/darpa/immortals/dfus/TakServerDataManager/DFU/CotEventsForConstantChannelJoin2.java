package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;

public class CotEventsForConstantChannelJoin2 {

	public CachedRowSet execute(PGPoolingDataSource dataSource) throws Exception {

		String query = "select ce.id, ce.cot_type, ce.servertime " + 
				"from source s join cot_event ce on s.id = ce.source_id " + 
				"where channel != 7";
	
		CachedRowSet rowset = null;
		
		try (	Connection conn = dataSource.getConnection();
				ResultSet rs = conn.prepareStatement(query).executeQuery()) {
			rowset = RowSetProvider.newFactory().createCachedRowSet();
			rowset.populate(rs);
		}
		
		return rowset;

	}
}
