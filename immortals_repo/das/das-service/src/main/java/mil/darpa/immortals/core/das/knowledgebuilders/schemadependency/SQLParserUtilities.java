package mil.darpa.immortals.core.das.knowledgebuilders.schemadependency;

import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.schema.Column;

public class SQLParserUtilities {

	public static boolean isLiteral(Object node) {
		
		Class<?> c = node.getClass();
		
		if (c.equals(StringValue.class) ||
				c.equals(TimeValue.class) ||
				c.equals(LongValue.class) ||
				c.equals(DoubleValue.class) ||
				c.equals(DateValue.class) ||
				c.equals(TimestampValue.class)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isColumn(Object node) {
		
		return node.getClass().equals(Column.class);
	}

	public static boolean isParameter(Object node) {
		
		return node.getClass().equals(JdbcParameter.class);
	}

}
