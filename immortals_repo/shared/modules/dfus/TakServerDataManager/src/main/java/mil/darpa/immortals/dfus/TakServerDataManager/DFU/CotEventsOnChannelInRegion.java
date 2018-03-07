package mil.darpa.immortals.dfus.TakServerDataManager.DFU;

import java.sql.Connection;
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
public class CotEventsOnChannelInRegion {

    @FunctionalAspectAnnotation(
            aspect = AspectQuery.class,
            resourceDependencyUris = DataDFU.COT_SCHEMA,
            aspectSpecificResourceDependencies = DBSchema.class
    )
	public CachedRowSet execute(PGPoolingDataSource dataSource) throws Exception {

		String query = "select s.name, ce.id, ce.cot_type, ce.servertime " + 
				"from source s join cot_event ce on s.source_id = ce.source_id " + 
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
