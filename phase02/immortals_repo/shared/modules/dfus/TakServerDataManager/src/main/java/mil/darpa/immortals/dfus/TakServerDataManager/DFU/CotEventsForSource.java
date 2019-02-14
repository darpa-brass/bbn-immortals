package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;

public class CotEventsForSource {

	public CachedRowSet execute(PGPoolingDataSource dataSource, int sourceId) throws Exception {

		if (sourceId < 0) {
			throw new Exception("Invalid sourceId argument.");
		}

		String query = "select s.source_id, ce.servertime, cep.tilex, cep.tiley " + 
				"from source s join cot_event ce on s.source_id = ce.source_id " + 
				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
				"where s.source_id = ?";
		
		CachedRowSet rowset = null;
		ResultSet rs = null;
		
		try (	Connection conn = dataSource.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, String.valueOf(sourceId));

			rs = stmt.executeQuery();

			rowset = RowSetProvider.newFactory().createCachedRowSet();
			rowset.populate(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		
		return rowset;
	}
}
