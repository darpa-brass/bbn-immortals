package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLTransformerTest {

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
	public void test1() {

		String sql = "select a, b, c from table1 where a = ? and b = '1'";
		String fixedSql = "select a, b, c, a as p__0 from table1 where TRUE and b = '1'";
		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0 from (" + fixedSql + ") as __t1 group by p__0 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void test2() {

		String sql = "select a, b, c from table1 where table1.a = ? and b = '1'";
		String fixedSql = "select a, b, c, table1.a as p__0 from table1 where true and b = '1'";
		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0 from (" + fixedSql + ") as __t1 group by p__0 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void test3() {

		String sql = "select a, b, c from table1 where a = '1' and b = ?";
		String fixedSql = "select a, b, c, b as p__0 from table1 where a = '1' and true";
		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0 from (" + fixedSql + ") as __t1 group by p__0 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void test4() {

		String sql = "select a, b, c from table1 where a = ? and b = ?";
		String fixedSql = "select a, b, c, a as p__0, b as p__1 from table1 where true and true";
		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0, p__1 from (" + fixedSql + ") as __t1 group by p__0, p__1 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void test5() {

		String sql = "select a, b, c from table1 where ((a = ? and b = 1) or (table1.c = 'xyz' and d = ?)) or table1.e = ?";
		String fixedSql = "select a, b, c, a as p__0, d as p__1, table1.e as p__2 from table1 where ((true and b = 1) or (table1.c = 'xyz' and true)) or true";
		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0, p__1, p__2 from (" + fixedSql + ") as __t1 group by p__0, p__1, p__2 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void test6() {

		String sql = "select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley " + 
				"from source s join cot_event ce on s.source_id = ce.source_id " + 
				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
				"where s.name = ? and servertime = ?";

		String fixedSql = "select s.source_id, s.name, ce.id, ce.servertime, cep.tilex, cep.tiley, s.name as p__0, servertime as p__1 " + 
				"from source s join cot_event ce on s.source_id = ce.source_id " + 
				"join cot_event_position cep on ce.id = cep.cot_event_id " + 
				"where true and true";

		
		SQLTransformer st = new SQLTransformer();
		
		String result = null;
		
		try {
			result = st.getParameterDistributionSQL(sql);
			assert(result.equalsIgnoreCase("select p__0, p__1 from (" + fixedSql + ") as __t1 group by p__0, p__1 order by count(*) desc"));
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}
	
	@Test
	public void testParamerizeSQLSimpleString() {
		
		String sql = "select t1.column1 from table1 t1 where t1.column2 = 'test'";

		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 1);
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("t1.column2"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLSimpleLong() {
		
		String sql = "select t1.column1 from table1 t1 where t1.column2 = 5";

		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 1);
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("t1.column2"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLMultipleLiterals() {
		
		String sql = "select t1.column1 from table1 t1 where t1.column2 = 5 and t1.column3 = 'test'";

		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 2);
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("t1.column2"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);
			assert(sqlMetadata.getParameters().get(1).getColumnName().equals("t1.column3"));
			assert(sqlMetadata.getParameters().get(1).getOrdinalPosition() == 2);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLIgnoreLiteralsInProject() {
		
		String sql = "select source_id, 'output' as alias1 from takrpt.source where source_id = 222";

		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 1);
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("source_id"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLComplexExpression() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
		 "servertime = '201705071635') or not (how = 'm-g')";

		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 4);
			
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("cot_type"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);

			assert(sqlMetadata.getParameters().get(1).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(1).getOrdinalPosition() == 2);

			assert(sqlMetadata.getParameters().get(2).getColumnName().equals("servertime"));
			assert(sqlMetadata.getParameters().get(2).getOrdinalPosition() == 3);

			assert(sqlMetadata.getParameters().get(3).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(3).getOrdinalPosition() == 4);

		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLComplexExpressionWithMask() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
		 "servertime = '201705071635') or not (how = 'm-g')";

		SQLTransformer st = new SQLTransformer();
		int mask = (1 << 1) | (1 << 3); //parameterize the 2nd and 4th literal
		
		try {
			String psql = st.parameterizeSql(sql, mask);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 2);
			
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);

			assert(sqlMetadata.getParameters().get(1).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(1).getOrdinalPosition() == 2);

		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLComplexExpressionWithMaskAll() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
		 "servertime = '201705071635') or not (how = 'm-g')";

		SQLTransformer st = new SQLTransformer();
		//Select all literals
		int mask = ~0;
						
		try {
			String psql = st.parameterizeSql(sql, mask);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 4);
			
			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("cot_type"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);

			assert(sqlMetadata.getParameters().get(1).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(1).getOrdinalPosition() == 2);

			assert(sqlMetadata.getParameters().get(2).getColumnName().equals("servertime"));
			assert(sqlMetadata.getParameters().get(2).getOrdinalPosition() == 3);

			assert(sqlMetadata.getParameters().get(3).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(3).getOrdinalPosition() == 4);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLComplexExpressionWithMaskNone() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
		 "servertime = '201705071635') or not (how = 'm-g')";

		SQLTransformer st = new SQLTransformer();
		//Select no literals
		int mask = 0;
						
		try {
			String psql = st.parameterizeSql(sql, mask);
			assertNotNull(psql);
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 0);			
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testParamerizeSQLComplexExpressionFlipBinary() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where (('a-n-A-C-F-m' = cot_type and how = 'm-g') or " +
		 "servertime = '201705071635') or not ('m-g' = how)";

		SQLTransformer st = new SQLTransformer();
						
		try {
			String psql = st.parameterizeSql(sql, -1);
			String expectedValue = "SELECT id, cot_type, how FROM takrptbase.cot_event " +
					"WHERE (( ?  = cot_type AND how =  ? ) OR servertime =  ? ) OR NOT ( ?  = how)";
			assert(psql.equals(expectedValue));
			SQLMetadata sqlMetadata = SQLMetadata.buildSqlMetadata(psql);
			assert(sqlMetadata.getParameters().size() == 4);			

			assert(sqlMetadata.getParameters().get(0).getColumnName().equals("cot_type"));
			assert(sqlMetadata.getParameters().get(0).getOrdinalPosition() == 1);

			assert(sqlMetadata.getParameters().get(1).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(1).getOrdinalPosition() == 2);

			assert(sqlMetadata.getParameters().get(2).getColumnName().equals("servertime"));
			assert(sqlMetadata.getParameters().get(2).getOrdinalPosition() == 3);

			assert(sqlMetadata.getParameters().get(3).getColumnName().equals("how"));
			assert(sqlMetadata.getParameters().get(3).getOrdinalPosition() == 4);
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}

	@Test
	public void testGenerateParamCombinations() {
		
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
		 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
		 "servertime = '201705071635') or not (how = 'm-g')";

		SQLTransformer st = new SQLTransformer();
		int numberParameters = 2;
		int numberLiterals = 4;
		List<String> combinations = null;
		List<String> expected = new ArrayList<>(6);
		
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type = 'a-n-A-C-F-m' AND how = 'm-g') OR servertime =  ? ) OR NOT (how =  ? )");
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type = 'a-n-A-C-F-m' AND how =  ? ) OR servertime = '201705071635') OR NOT (how =  ? )");
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type =  ?  AND how = 'm-g') OR servertime = '201705071635') OR NOT (how =  ? )");
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type = 'a-n-A-C-F-m' AND how =  ? ) OR servertime =  ? ) OR NOT (how = 'm-g')");
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type =  ?  AND how = 'm-g') OR servertime =  ? ) OR NOT (how = 'm-g')");
		expected.add("SELECT id, cot_type, how FROM takrptbase.cot_event WHERE ((cot_type =  ?  AND how =  ? ) OR servertime = '201705071635') OR NOT (how = 'm-g')");

		try {
			combinations = st.generateParameterizedCombinations(numberParameters, sql, numberLiterals);

			assert(combinations.size() == computeCombination(numberLiterals, numberParameters));
			for (String e : expected) {
				assert(combinations.contains(e));
			}
		} catch (Exception e) {
			fail("Test case failed due to unexpected error: " + e.getMessage());
		}
	}
	
	@Test
	public void testResolveParameters() {
		
		DataLinkageMetadata dlm = new DataLinkageMetadata();
		String sql = "select id, cot_type, how from takrptbase.cot_event " +
				 "where ((cot_type = 'a-n-A-C-F-m' and how = 'm-g') or " +
				 "servertime = '201705071635') or not (how = 'm-g')";
		
		SQLTransformer st = new SQLTransformer();
		
		try {
			String psql = st.parameterizeSql(sql, -1);
			SQLMetadata sm = SQLMetadata.buildSqlMetadata(psql);
			sm.getParameters().get(0).setValue("'a-n-A-C-F-m'");
			sm.getParameters().get(1).setValue("'m-g'");
			sm.getParameters().get(2).setValue("'201705071635'");
			sm.getParameters().get(3).setValue("'m-g'");
			dlm.setOriginalSql(psql);
			dlm.setSqlMetadata(sm);
			
			List<Integer> order = new ArrayList<Integer>();
			order.add(1);
			order.add(2);
			order.add(0);
			order.add(3);
			
			String resolvedSql = st.resolveParameters(dlm, order);
			
			SQLMetadata sm2 = SQLMetadata.buildSqlMetadata(resolvedSql);

			sm2.getLiteralReferencesInFilter().get(0).getValue().toString().equals("'m-g'");
			sm2.getLiteralReferencesInFilter().get(1).getValue().toString().equals("'201705071635'");
			sm2.getLiteralReferencesInFilter().get(2).getValue().toString().equals("'a-n-A-C-F-m'");
			sm2.getLiteralReferencesInFilter().get(3).getValue().toString().equals("'m-g'");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		
	}
		
	private long computeCombination(int n, int k) {
		
		long result = 0;
		
		if(k > n) {
			result = 0;
		} else if (k == 0 || k == n) {
			//Base cases
			result = 1;
		} else if (k > n/2) {
			//Combinations are symmetric
			result = computeCombination(n,n-k);
		} else {
			result = computeCombination(n-1, k-1) + computeCombination(n-1, k);
		}

		return result;
		
	}

	

}
