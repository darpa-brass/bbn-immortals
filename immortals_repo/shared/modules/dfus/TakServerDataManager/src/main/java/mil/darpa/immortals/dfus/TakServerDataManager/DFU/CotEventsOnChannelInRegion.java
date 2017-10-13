package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;

public class CotEventsOnChannelInRegion {

	public CachedRowSet execute(PGPoolingDataSource dataSource) throws Exception {

		String query = "select s.name, ce.id, ce.cot_type, ce.servertime " + 
				"from source s join cot_event ce on s.id = ce.source_id " + 
				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
				"where  s.channel = 6 and tilex = 18830 and tiley = 25704";
	
		CachedRowSet rowset = null;
		
		try (	Connection conn = dataSource.getConnection();
				ResultSet rs = conn.prepareStatement(query).executeQuery()) {
			rowset = RowSetProvider.newFactory().createCachedRowSet();
			rowset.populate(rs);
		}
		
		return rowset;

	}
}
