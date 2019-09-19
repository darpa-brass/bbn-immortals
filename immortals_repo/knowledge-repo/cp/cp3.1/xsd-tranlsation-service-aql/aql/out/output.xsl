<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <xsl:param name="targetMDLVersion" select="'http://inetprogram.org/projects/MDL/ MDL_v0_8_19.xsd'"/>
  <xsl:template match="/mdl:MDLRoot/@xsi:schemaLocation">
    <xsl:attribute name="xsi:schemaLocation">
      <xsl:value-of select="$targetMDLVersion"/>
    </xsl:attribute>
  </xsl:template>
  <!-- identity template, copies everything as is -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <!-- RENAME node <ModelNumber> into <Model> -->
  <xsl:template match="//mdl:ModelNumber">
    <mdl:Model>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:Model>
  </xsl:template>
  <!-- RENAME node <SerialNumber> into <SerialIdentifier> -->
  <xsl:template match="//mdl:SerialNumber">
    <mdl:SerialIdentifier>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:SerialIdentifier>
  </xsl:template>
  <!-- RENAME node <NetworkInterface> into <InternalStructure> -->
  <xsl:template match="//mdl:NetworkInterface">
    <mdl:InternalStructure>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:InternalStructure>
  </xsl:template>
  <!-- RENAME node <Routes> into <InternalStructure> -->
  <xsl:template match="//mdl:Routes">
    <mdl:InternalStructure>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:InternalStructure>
  </xsl:template>
  <!-- RENAME node <Connector> into <InternalStructure> -->
  <xsl:template match="//mdl:Connector">
    <mdl:InternalStructure>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:InternalStructure>
  </xsl:template>
  <!-- RENAME node <Ports> into <InternalStructure> -->
  <xsl:template match="//mdl:Ports">
    <mdl:InternalStructure>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:InternalStructure>
  </xsl:template>
  <!-- RENAME node <Calibration> into <DeviceStructure> -->
  <xsl:template match="//mdl:Calibration">
    <mdl:DeviceStructure>
      <xsl:apply-templates select="@*|node()"/>
    </mdl:DeviceStructure>
  </xsl:template>
  <!-- REMOVE node RadioLinkType/LinkControlMode -->
  <xsl:template match="//mdl:RadioLink/mdl:LinkControlMode"/>
  <!-- REMOVE node VendorConfigType/NameValue -->
  <xsl:template match="//mdl:VendorConfig/mdl:NameValue"/>
  <!-- REMOVE node TmNSDAUType/Module -->
  <xsl:template match="//mdl:TmNSDAU/mdl:Module"/>
  <!-- REMOVE node GenericParameterType/NameValue -->
  <xsl:template match="//mdl:GenericParameter/mdl:NameValue"/>
  <!-- REMOVE node NetworkNodeType/InternalStructure -->
  <xsl:template match="//mdl:NetworkNode/mdl:InternalStructure"/>
</xsl:stylesheet>
