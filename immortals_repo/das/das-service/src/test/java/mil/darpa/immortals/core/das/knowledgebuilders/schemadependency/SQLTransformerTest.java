package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import static org.junit.Assert.*;

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

}
