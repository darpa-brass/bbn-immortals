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
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataOperations/DataOperation/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataOperations/mdl:DataOperation/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataOperations/mdl:DataOperation/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataOperations/mdl:DataOperation/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataOperations/mdl:DataOperation/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffset" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffsetIncrement" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffset" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffsetIncrement" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffset" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffsetIncrement" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/PCMDataLink/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:PCMDataLink/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:PCMDataLink/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:PCMDataLink/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:PCMDataLink/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/TimeAttributes" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeAttributes" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="TimestampFormat" namespace="http://inetprogram.org/projects/MDL"/>
        <!--CHECK! Created with first element of restriction from type spec-->
        <xsl:element name="TimestampType" namespace="http://inetprogram.org/projects/MDL">AbsoluteTime</xsl:element>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/MeasurementTimeRef" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="MeasurementTimeRef" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/ProperName" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="ProperName" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Messages/MessageDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Messages/mdl:MessageDefinition/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Messages/mdl:MessageDefinition/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Messages/mdl:MessageDefinition/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Messages/mdl:MessageDefinition/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffset" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffsetIncrement" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffset" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="TimeOffsetIncrement" namespace="http://inetprogram.org/projects/MDL"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/NonTmNSPackageHeaderFields/FieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:NonTmNSPackageHeaderFields/mdl:FieldDescription/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:NonTmNSPackageHeaderFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:NonTmNSPackageHeaderFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:NonTmNSPackageHeaderFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PDIDFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PDIDFieldDescription/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PDIDFieldDescription/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PDIDFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PDIDFieldDescription/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageLengthField/PackageLengthFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageLengthField/mdl:PackageLengthFieldDescription/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageLengthField/mdl:PackageLengthFieldDescription/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageLengthField/mdl:PackageLengthFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageLengthField/mdl:PackageLengthFieldDescription/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/TimestampDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:TimestampDefinition/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:TimestampDefinition/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:TimestampDefinition/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:TimestampDefinition/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/StatusFlagFields/FieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:StatusFlagFields/mdl:FieldDescription/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:StatusFlagFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:StatusFlagFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:StatusFlagFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/N2NPortMapping/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/DeviceStructure" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="DeviceStructure" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
              <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
            </xsl:if>
            <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
              <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:if>
          </xsl:element>
        </xsl:element>
        <xsl:element name="DeviceModule" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
              </xsl:if>
              <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:if>
            </xsl:element>
          </xsl:element>
          <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
              </xsl:if>
              <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:if>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Manufacturer" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Model" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="SerialIdentifier" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="InventoryID" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Position" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="PositionsOccupied" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="DataOperationRef" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Sensitivity" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
              <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Excitation" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
              <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
            </xsl:element>
          </xsl:element>
          <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Calibration">
            <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Calibration"/>
          </xsl:if>
          <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Calibration)">
            <xsl:element name="Calibration" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="CalibrationDate" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="CalibrationPair" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="InputValue" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FlexValue" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FloatValue" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                </xsl:element>
                <xsl:element name="OutputValue" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FlexValue" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FloatValue" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                </xsl:element>
              </xsl:element>
            </xsl:element>
          </xsl:if>
          <xsl:element name="Children" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Child" namespace="http://inetprogram.org/projects/MDL">
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="ChildRef" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Connector" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Pin" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Designator" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="DataOperationRefs" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef)">
                  <xsl:element name="DataOperationRef" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Ports" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Port" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="PortDirection" namespace="http://inetprogram.org/projects/MDL">Input</xsl:element>
              <xsl:element name="PortType" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="AnalogAttributes" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="EUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="IUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="Resolution" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="FrequencyContent" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FrequencyBand" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="BandType" namespace="http://inetprogram.org/projects/MDL">PassBand</xsl:element>
                    <xsl:element name="EdgeFrequency" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                        <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                      </xsl:element>
                    </xsl:element>
                    <xsl:element name="Attenuation" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                        <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                      </xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="PercentSignal" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:element>
              </xsl:element>
              <xsl:element name="Excitation" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                  <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                </xsl:element>
              </xsl:element>
              <xsl:element name="PinRef" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="PhysicalNetworkPortRef" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:element>
          </xsl:element>
          <xsl:element name="DeviceSubModule" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                  <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
            <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                  <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Manufacturer" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Model" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="SerialIdentifier" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="InventoryID" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Position" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="PositionsOccupied" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Children" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Child" namespace="http://inetprogram.org/projects/MDL">
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="ChildRef" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Connector" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Pin" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Designator" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="DataOperationRefs" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef)">
                    <xsl:element name="DataOperationRef" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Ports" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Port" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <!--CHECK! Created with first element of restriction from type spec-->
                <xsl:element name="PortDirection" namespace="http://inetprogram.org/projects/MDL">Input</xsl:element>
                <xsl:element name="PortType" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="AnalogAttributes" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="EUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="IUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="Resolution" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="FrequencyContent" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="FrequencyBand" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="BandType" namespace="http://inetprogram.org/projects/MDL">PassBand</xsl:element>
                      <xsl:element name="EdgeFrequency" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                          <!--Created with default value-->
                          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                          <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                        </xsl:element>
                      </xsl:element>
                      <xsl:element name="Attenuation" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                          <!--Created with default value-->
                          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                          <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                        </xsl:element>
                      </xsl:element>
                    </xsl:element>
                    <xsl:element name="PercentSignal" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="Excitation" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="PinRef" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="PhysicalNetworkPortRef" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:LogicalLocation/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/PhysicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:PhysicalLocation/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/VendorConfig/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:VendorConfig">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:VendorConfig/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:VendorConfig/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:VendorConfig/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
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
        <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
              <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
            </xsl:if>
            <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
              <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:if>
          </xsl:element>
        </xsl:element>
        <xsl:element name="Module" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
              </xsl:if>
              <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:if>
            </xsl:element>
          </xsl:element>
          <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
              </xsl:if>
              <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:if>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Manufacturer" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Model" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="SerialIdentifier" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="InventoryID" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Position" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="PositionsOccupied" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="RunningApps" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="ManageableAppRef" namespace="http://inetprogram.org/projects/MDL"/>
          </xsl:element>
          <xsl:element name="NetworkInterface" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                  <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
            <xsl:element name="DHCPEnable" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="IPAddress" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Netmask" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Gateway" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="MACAddress" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="PhysicalNetworkPort" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="Medium" namespace="http://inetprogram.org/projects/MDL">Copper</xsl:element>
              <xsl:element name="PortNumber" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="PortDataRate" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="FloatValue" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                <!--CHECK! Created with first element of restriction from type spec-->
                <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
              </xsl:element>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="IEEE1588VersionOfPort" namespace="http://inetprogram.org/projects/MDL">2002</xsl:element>
            </xsl:element>
          </xsl:element>
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
                <xsl:element name="Destination" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Netmask" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Gateway" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Metric" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NetworkInterfaceRef" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="RadioLinkRef" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
          </xsl:if>
          <xsl:element name="Children" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Child" namespace="http://inetprogram.org/projects/MDL">
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="ChildRef" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Connector" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Pin" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Designator" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="DataOperationRefs" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef)">
                  <xsl:element name="DataOperationRef" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
          </xsl:element>
          <xsl:element name="Ports" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Port" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                    <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
              <!--CHECK! Created with first element of restriction from type spec-->
              <xsl:element name="PortDirection" namespace="http://inetprogram.org/projects/MDL">Input</xsl:element>
              <xsl:element name="PortType" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="AnalogAttributes" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="EUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="IUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="Resolution" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="FrequencyContent" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="FrequencyBand" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="BandType" namespace="http://inetprogram.org/projects/MDL">PassBand</xsl:element>
                    <xsl:element name="EdgeFrequency" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                        <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                      </xsl:element>
                    </xsl:element>
                    <xsl:element name="Attenuation" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                        <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                        <!--CHECK! Created with first element of restriction from type spec-->
                        <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                      </xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="PercentSignal" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:element>
              </xsl:element>
              <xsl:element name="Excitation" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                  <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                  <!--CHECK! Created with first element of restriction from type spec-->
                  <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                </xsl:element>
              </xsl:element>
              <xsl:element name="PinRef" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="PhysicalNetworkPortRef" namespace="http://inetprogram.org/projects/MDL"/>
            </xsl:element>
          </xsl:element>
          <xsl:element name="SubModule" namespace="http://inetprogram.org/projects/MDL">
            <!--Created with default value-->
            <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
            <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                  <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
            <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                  <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                </xsl:if>
                <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                  <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                </xsl:if>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Manufacturer" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Model" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="SerialIdentifier" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="InventoryID" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Position" namespace="http://inetprogram.org/projects/MDL"/>
            <xsl:element name="Children" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Child" namespace="http://inetprogram.org/projects/MDL">
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="ChildRef" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Connector" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Pin" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Designator" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="DataOperationRefs" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef">
                    <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef"/>
                  </xsl:if>
                  <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef)">
                    <xsl:element name="DataOperationRef" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:if>
                </xsl:element>
              </xsl:element>
            </xsl:element>
            <xsl:element name="Ports" namespace="http://inetprogram.org/projects/MDL">
              <!--Created with default value-->
              <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
              <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
              <xsl:element name="Port" namespace="http://inetprogram.org/projects/MDL">
                <!--Created with default value-->
                <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="GenericParameter" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="VendorConfig" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue">
                      <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
                    </xsl:if>
                    <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue)">
                      <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
                    </xsl:if>
                  </xsl:element>
                </xsl:element>
                <!--CHECK! Created with first element of restriction from type spec-->
                <xsl:element name="PortDirection" namespace="http://inetprogram.org/projects/MDL">Input</xsl:element>
                <xsl:element name="PortType" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="AnalogAttributes" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="EUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="IUSignalRange" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="Resolution" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                      <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                      <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                    </xsl:element>
                  </xsl:element>
                  <xsl:element name="FrequencyContent" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="FrequencyBand" namespace="http://inetprogram.org/projects/MDL">
                      <!--Created with default value-->
                      <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                      <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                      <!--CHECK! Created with first element of restriction from type spec-->
                      <xsl:element name="BandType" namespace="http://inetprogram.org/projects/MDL">PassBand</xsl:element>
                      <xsl:element name="EdgeFrequency" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                          <!--Created with default value-->
                          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                          <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                        </xsl:element>
                      </xsl:element>
                      <xsl:element name="Attenuation" namespace="http://inetprogram.org/projects/MDL">
                        <!--Created with default value-->
                        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                        <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                          <!--Created with default value-->
                          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                          <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                          <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                          <!--CHECK! Created with first element of restriction from type spec-->
                          <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                        </xsl:element>
                      </xsl:element>
                    </xsl:element>
                    <xsl:element name="PercentSignal" namespace="http://inetprogram.org/projects/MDL"/>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="Excitation" namespace="http://inetprogram.org/projects/MDL">
                  <!--Created with default value-->
                  <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                  <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                  <xsl:element name="ConditionParameter" namespace="http://inetprogram.org/projects/MDL">
                    <!--Created with default value-->
                    <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
                    <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="ConditionOperation" namespace="http://inetprogram.org/projects/MDL">&gt;</xsl:element>
                    <xsl:element name="ConditionValueFlex" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="ConditionValueFloat" namespace="http://inetprogram.org/projects/MDL"/>
                    <xsl:element name="UnitsRef" namespace="http://inetprogram.org/projects/MDL"/>
                    <!--CHECK! Created with first element of restriction from type spec-->
                    <xsl:element name="SIUnits" namespace="http://inetprogram.org/projects/MDL">Ampere</xsl:element>
                  </xsl:element>
                </xsl:element>
                <xsl:element name="PinRef" namespace="http://inetprogram.org/projects/MDL"/>
                <xsl:element name="PhysicalNetworkPortRef" namespace="http://inetprogram.org/projects/MDL"/>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:LogicalLocation/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/PhysicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:PhysicalLocation/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/VendorConfig/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:VendorConfig">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:VendorConfig/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:VendorConfig/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:VendorConfig/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/VendorConfig/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:VendorConfig">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:VendorConfig/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:VendorConfig/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:VendorConfig/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/PortMappings/PortMapping/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:PortMappings/mdl:PortMapping/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:PortMappings/mdl:PortMapping/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:PortMappings/mdl:PortMapping/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:PortMappings/mdl:PortMapping/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/TestMissions/TestMission/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/TestMissions/TestMission/HandoffRules" -->
  <xsl:template match="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="HandoffRules" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:element name="HandoffRule" namespace="http://inetprogram.org/projects/MDL">
          <!--Created with default value-->
          <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
          <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Name" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Description" namespace="http://inetprogram.org/projects/MDL"/>
          <xsl:element name="Rule" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:element>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/Units/DerivedUnit/GenericParameter/NameValues" -->
  <xsl:template match="/mdl:MDLRoot/mdl:Units/mdl:DerivedUnit/mdl:GenericParameter">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:element name="NameValues" namespace="http://inetprogram.org/projects/MDL">
        <!--Created with default value-->
        <xsl:element name="ReadOnly" namespace="http://inetprogram.org/projects/MDL">false</xsl:element>
        <xsl:element name="Owner" namespace="http://inetprogram.org/projects/MDL"/>
        <xsl:if test="/mdl:MDLRoot/mdl:Units/mdl:DerivedUnit/mdl:GenericParameter/mdl:NameValue">
          <xsl:copy-of select="/mdl:MDLRoot/mdl:Units/mdl:DerivedUnit/mdl:GenericParameter/mdl:NameValue"/>
        </xsl:if>
        <xsl:if test="not(/mdl:MDLRoot/mdl:Units/mdl:DerivedUnit/mdl:GenericParameter/mdl:NameValue)">
          <xsl:element name="NameValue" namespace="http://inetprogram.org/projects/MDL"/>
        </xsl:if>
      </xsl:element>
    </xsl:copy>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Antenna/ModelNumber" to "Model"-->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Antenna/mdl:ModelNumber">
    <xsl:element name="Model">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Antenna/SerialNumber" to "SerialIdentifier"-->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Antenna/mdl:SerialNumber">
    <xsl:element name="SerialIdentifier">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Device/ModelNumber" to "Model"-->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:ModelNumber">
    <xsl:element name="Model">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Device/SerialNumber" to "SerialIdentifier"-->
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:SerialNumber">
    <xsl:element name="SerialIdentifier">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- handle removals-->
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataOperations/mdl:DataOperation/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:ARINC429Messages/mdl:ARINC429Message/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericDataStreamMessages/mdl:GenericDataStreamMessage/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:DataStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:MILSTD1553Messages/mdl:MILSTD1553Message/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:DataStreams/mdl:DataStream/mdl:PCMDataLink/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:DataAttributes/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Measurements/mdl:Measurement/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Messages/mdl:MessageDefinition/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:DataMap/mdl:DataWordToFieldMap/mdl:DataWord/mdl:Syllable/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:NonTmNSPackageHeaderFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PDIDFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageLengthField/mdl:PackageLengthFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:PackageTimeDeltaFieldDescription/mdl:TimestampDefinition/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageDefinition/mdl:PackageHeaderDefinition/mdl:PackageHeaderStructure/mdl:StatusFlagFields/mdl:FieldDescription/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:MeasurementDomains/mdl:MeasurementDomain/mdl:Packages/mdl:PackageStructure/mdl:PackageDataFieldSet/mdl:PackageDataField/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:N2NPortMapping/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Calibration"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Connector"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:DataOperationRef"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Excitation"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Ports"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:Sensitivity"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:Device/mdl:VendorConfig/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Connector"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:LogicalLocation/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:NetworkInterface"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:PhysicalLocation/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Ports"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:Routes"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:TmNSDAU/mdl:Module"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:TmNSManageableApps/mdl:TmNSManageableApp/mdl:VendorConfig/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:NetworkNode/mdl:VendorConfig/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:NetworkDomains/mdl:Network/mdl:PortMappings/mdl:PortMapping/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:GenericParameter/mdl:NameValue"/>
  <xsl:template match="/mdl:MDLRoot/mdl:TestMissions/mdl:TestMission/mdl:RadioLinks/mdl:RadioLink/mdl:LinkControlMode"/>
  <xsl:template match="/mdl:MDLRoot/mdl:Units/mdl:DerivedUnit/mdl:GenericParameter/mdl:NameValue"/>
</xsl:stylesheet>
