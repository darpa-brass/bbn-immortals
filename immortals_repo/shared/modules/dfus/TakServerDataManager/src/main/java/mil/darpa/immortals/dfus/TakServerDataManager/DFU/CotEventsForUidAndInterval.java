package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.postgresql.ds.PGPoolingDataSource;

import com.securboration.immortals.ontology.functionality.database.AspectQuery;
import com.securboration.immortals.ontology.functionality.database.ManageDatabase;
import com.securboration.immortals.ontology.resources.DiskResource;
import com.securboration.immortals.ontology.resources.logical.DBSchema;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

@DfuAnnotation(
        functionalityBeingPerformed = ManageDatabase.class,
        resourceDependencies = {
                DBSchema.class,
                DiskResource.class
        },
        resourceDependencyUris = {
                DataDFU.COT_SCHEMA
        }
)
public class CotEventsForUidAndInterval {

    @FunctionalAspectAnnotation(
            aspect = AspectQuery.class,
            resourceDependencyUris = DataDFU.COT_SCHEMA,
            aspectSpecificResourceDependencies = DBSchema.class
    )
	public CachedRowSet execute(PGPoolingDataSource dataSource, String sourceName, Long timeInterval) throws Exception {

		if (sourceName == null || sourceName.trim().length() == 0) {
			throw new Exception("Invalid sourceName argument.");
		}

		if (timeInterval == null || timeInterval < 0) {
			throw new Exception("Invalid timeInterval argument.");
		}
		
		String query = "select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley " + 
				"from source s join cot_event ce on s.source_id = ce.source_id " + 
				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
				"where s.name = ? and servertime = ?";
	
		CachedRowSet rowset = null;
		ResultSet rs = null;
		
		try (	Connection conn = dataSource.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, sourceName);
			stmt.setString(2, String.valueOf(timeInterval));

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
