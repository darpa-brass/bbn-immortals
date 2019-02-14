package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;

public class SQLMetadataTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleStringLiteral() {

		String sql = "select t1.column1 from table1 t1 where t1.column2 = 'test'";
		
		try {
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(sql);
			assert(sqlMetadata.getLiteralReferencesInFilter().size() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getColumn().equals("t1.column2"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getPosition() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().toString().equals("'test'"));
		} catch (Exception e) {
			fail("Unexpected error parsing sql metadata: " + e.getMessage());
		}
	}

	@Test
	public void testSimpleLongLiteral() {

		String sql = "select t1.column1 from table1 t1 where t1.column2 = 5";
		
		try {
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(sql);
			assert(sqlMetadata.getLiteralReferencesInFilter().size() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getColumn().equals("t1.column2"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getPosition() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().getClass().equals(LongValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().toString().equals("5"));
		} catch (Exception e) {
			fail("Unexpected error parsing sql metadata: " + e.getMessage());
		}
	}

	@Test
	public void testMultipleLiterals() {

		String sql = "select t1.column1 from table1 t1 where t1.column2 = 5 and t1.column3 = 'test'";
		
		try {
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(sql);
			assert(sqlMetadata.getLiteralReferencesInFilter().size() == 2);

			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getColumn().equals("t1.column2"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getPosition() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().getClass().equals(LongValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().toString().equals("5"));

			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getColumn().equals("t1.column3"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getPosition() == 2);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getValue().toString().equals("'test'"));
		} catch (Exception e) {
			fail("Unexpected error parsing sql metadata: " + e.getMessage());
		}
	}

	@Test
	public void testIgnoreLiteralsInProjection() {

		String sql = "select source_id, 'output' as alias1 from takrpt.source where source_id = 222";
		
		try {
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(sql);
			assert(sqlMetadata.getLiteralReferencesInFilter().size() == 1);

			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getColumn().equals("source_id"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getPosition() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().getClass().equals(LongValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().toString().equals("222"));
		} catch (Exception e) {
			fail("Unexpected error parsing sql metadata: " + e.getMessage());
		}
	}

	@Test
	public void testMultipleLiteralsInComplexExpression() {

		String sql = "select id, cot_type, how from takrptbase.cot_event where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or servertime = '201705071635') or not (how = 'm-g')";
		
		try {
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(sql);
			assert(sqlMetadata.getLiteralReferencesInFilter().size() == 4);

			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getColumn().equals("cot_type"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getPosition() == 1);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(0).getValue().toString().equals("'a-n-A-C-F-m'"));

			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getColumn().equals("how"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getPosition() == 2);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(1).getValue().toString().equals("'m-g'"));

			assert(sqlMetadata.getLiteralReferencesInFilter().get(2).getColumn().equals("servertime"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(2).getPosition() == 3);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(2).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(2).getValue().toString().equals("'201705071635'"));

			assert(sqlMetadata.getLiteralReferencesInFilter().get(3).getColumn().equals("how"));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(3).getPosition() == 4);
			assert(sqlMetadata.getLiteralReferencesInFilter().get(3).getValue().getClass().equals(StringValue.class));
			assert(sqlMetadata.getLiteralReferencesInFilter().get(3).getValue().toString().equals("'m-g'"));
		} catch (Exception e) {
			fail("Unexpected error parsing sql metadata: " + e.getMessage());
		}
	}

}
