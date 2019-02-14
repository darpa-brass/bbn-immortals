package com.securboration.demo;

public class Main {

	public static void main(String[] args) throws Exception {
		
		System.out.println(
				"A simple demo that illustrates issues that arise when a schema changes over time.\n" + 
				"Players:\n" +
				"  Client:       transmits XML to one of two servers\n" + 
				"  DataSource1:  generates documents conforming to schema v1\n" + 
				"  DataSource2:  generates documents conforming to schema v2\n" + 
				"  Server1:      expects to receive v1 documents\n" + 
				"  Server2:      expects to receive v2 documents\n" + 
				"Scenarios:\n" +
				"  Scenario1: client sends a v1 message to server1 (passes)\n" +
				"  Scenario2: client sends a v2 message to server2 (passes)\n" +
				"  Scenario3: client sends a v1 message to server2 (fails, adaptation needed)\n" +
				"  Scenario4: client sends a v2 message to server1 (fails, adaptation needed)\n" +
				"  Scenario5: client sends a v1 message to server1 WITH an XSLT transformation applied (works)\n"
				);
		
		final Client client = new Client();
		
		final Server server1 = new Server1();
		final Server server2 = new Server2();
		
		final DataSource ds1 = new DataSource1();
		final DataSource ds2 = new DataSource2();
		
		System.out.println("\nScenario I: client sends a v1 message to server1");
		try{
			client.sendMessage(server1,ds1.generateData());
		}catch(Exception e) {
			e.printStackTrace(System.out);
			
			System.out.println("ADAPTATION NEEDED (scenario I)");
		}
		
		System.out.println("\nScenario II: client sends a v2 message to server2");
		try{
			client.sendMessage(server2,ds2.generateData());
		}catch(Exception e) {
			e.printStackTrace(System.out);
			
			System.out.println("ADAPTATION NEEDED (scenario II)");
		}
		
		System.out.println("\nScenario III: client sends a v1 message to server2");
		try{
			client.sendMessage(server2,ds1.generateData());
		}catch(Exception e) {
			e.printStackTrace(System.out);
			
			System.out.println("ADAPTATION NEEDED (scenario III)");
		}
		
		System.out.println("\nScenario IV: client sends a v2 message to server1");
		try{
			client.sendMessage(server1,ds2.generateData());
		}catch(Exception e) {
			e.printStackTrace(System.out);
			
			System.out.println("ADAPTATION NEEDED (scenario IV)");
		}
		
		System.out.println("\nScenario V: client sends a v1 message to server2 WITH an XSLT transformation");
		try{
			client.sendMessage(server2,new XsltTransformer(v1Tov2Xslt).translate(ds1.generateData()));
		}catch(Exception e) {
			e.printStackTrace(System.out);
			
			System.out.println("ADAPTATION NEEDED (scenario V)");
		}
	}
	
	
	
	private static final String v1Example = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n" + 
			"<todolist xmlns=\"http://xmlbeans.apache.org/samples/validation/todolist\">\r\n" + 
			"    <item id=\"0\">\r\n" + 
			"        <name>TODOlist item 0</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358130</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"1\">\r\n" + 
			"        <name>TODOlist item 1</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"2\">\r\n" + 
			"        <name>TODOlist item 2</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"3\">\r\n" + 
			"        <name>TODOlist item 3</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"4\">\r\n" + 
			"        <name>TODOlist item 4</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"5\">\r\n" + 
			"        <name>TODOlist item 5</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"6\">\r\n" + 
			"        <name>TODOlist item 6</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"7\">\r\n" + 
			"        <name>TODOlist item 7</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"8\">\r\n" + 
			"        <name>TODOlist item 8</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"    <item id=\"9\">\r\n" + 
			"        <name>TODOlist item 9</name>\r\n" + 
			"        <description>an item in a TODOlist</description>\r\n" + 
			"        <due_by>1534425358131</due_by>\r\n" + 
			"        <action>someday_maybe_defer</action>\r\n" + 
			"    </item>\r\n" + 
			"</todolist>";
	
	private static final String noopXslt = 
			"<xsl:stylesheet version=\"1.0\"\r\n" + 
			"xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\r\n" + 
			"\r\n" + 
			"<xsl:template match=\"/\" />\r\n" + 
			"\r\n" + 
			"</xsl:stylesheet>";
	
	//example of XSLT that would be generated by Vanderbilt
	private static final String v1Tov2Xslt = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:tdl=\"http://xmlbeans.apache.org/samples/validation/todolist\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\r\n" + 
			"    <xsl:output indent=\"yes\"/>\r\n" + 
			"	<xsl:template match=\"tdl:todolist\">\r\n" + 
			"		<tdl:todolist>\r\n" + 
			"\r\n" + 
			"			<xsl:for-each select=\"tdl:item\">\r\n" + 
			"				<tdl:item>\r\n" + //<xsl:value-of select=\"name\"/>
			"                   <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>\r\n" +//TODO
			"					<tdl:name><xsl:value-of select=\"tdl:name\"/></tdl:name>\r\n" + 
			"					<tdl:description><xsl:value-of select=\"tdl:description\"/></tdl:description>\r\n" + 
			"					<tdl:due_by>\r\n" + 
			"						<xsl:call-template name=\"epochToDateTime\">\r\n" + 
			"							<xsl:with-param name=\"epochTime\" select=\"tdl:due_by\"/>\r\n" + 
			"						</xsl:call-template>\r\n" + 
			"					</tdl:due_by>\r\n" + 
			"					<tdl:action><xsl:value-of select=\"tdl:action\"/></tdl:action>\r\n" + 
			"				</tdl:item>\r\n" + 
			"			</xsl:for-each>\r\n" + 
			"		</tdl:todolist>\r\n" + 
			"	</xsl:template>\r\n" + 
			"	\r\n" + 
			"	<xsl:template name=\"epochToDateTime\">\r\n" + 
			"		<xsl:param name=\"epochTime\"/>\r\n" + 
			"		<xsl:value-of select=\"xs:dateTime('1970-01-01T00:00:00') + ($epochTime * xs:dayTimeDuration('PT0.001S'))\"/>\r\n" + 
			"	</xsl:template>\r\n" + 
			"	\r\n" + 
			"</xsl:stylesheet>";
	
}
