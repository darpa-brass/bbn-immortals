<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:tmats="https://wsmrc2vger.wsmr.army.mil/rcc/manuals/106-11" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <!-- identity template, copies everything as is -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <!--Remove schemaLocation attr-->
  <xsl:template match="mdl:MDLRoot/@xsi:schemaLocation"/>
  <!-- Add node "/MDLRoot/NetworkDomains/Domain" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="Domain" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/InternalStructure" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="InternalStructure" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="Module" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Routes">
            <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Routes"/>
          </xsl:if>
          <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Routes)">
            <xsl:element name="Routes" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Route" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Metric" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
          </xsl:if>
        </xsl:element>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/ConfigurationVersion" to "ConfigVersion"-->
  <xsl:template match="/mdl:MDLRoot/mdl:ConfigurationVersion">
    <xsl:element name="ConfigVersion">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- handle removals-->
  <xsl:template match="/mdl:MDLRoot/mdl:DatabaseID"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:NetworkName"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Routes"/>
  <!-- Moving element from "/MDLRoot/DatabaseID" to "/MDLRoot/NetworkDomains/DatabaseID"-->
  <xsl:template match="/mdl:MDLRoot/mdl:DatabaseID"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:copy-of select="/mdl:MDLRoot/mdl:DatabaseID"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
