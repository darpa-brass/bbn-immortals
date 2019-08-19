<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <!-- identity template, copies everything as is -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/InternalStructure" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode">
    <InternalStructure>
      <Description/>
      <Module>
        <Description/>
        <Name/>
        <Owner/>
        <ReadOnly/>
        <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/Routes"/>
      </Module>
      <Name/>
      <Owner/>
      <ReadOnly/>
    </InternalStructure>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Domain" -->
  <xsl:template match="/MDLRoot/NetworkDomains">
    <Domain/>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/ConfigurationVersion" to "ConfigVersion"-->
  <xsl:template match="/MDLRoot/ConfigurationVersion">
    <xsl:element name="ConfigVersion">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- handle removals-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/NetworkName"/>
  <!-- Moving element from "/MDLRoot/DatabaseID" to "/MDLRoot/NetworkDomains/DatabaseID"-->
  <xsl:template match="/MDLRoot/DatabaseID"/>
  <xsl:template match="/MDLRoot/NetworkDomains">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:copy-of select="/MDLRoot/DatabaseID"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>