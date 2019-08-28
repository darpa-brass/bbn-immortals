import os
from enum import Enum
from typing import Dict, List

from xmltest import config

_raw_config = None


class MdlVersion(Enum):
    UNKNOWN = 0
    # v0_8_1 = 81 # Not Included
    # v0_8_2 = 82 # Not Included
    # v0_8_3 = 83 # Not Included
    # v0_8_4 = 84 # Not Included
    # v0_8_5 = 85 # Not Included
    # v0_8_6 = 86 # Not Included
    v0_8_7 = 87
    v0_8_8 = 88
    v0_8_9 = 89
    v0_8_10 = 810
    v0_8_11 = 811
    v0_8_12 = 812
    v0_8_13 = 813
    v0_8_14 = 814
    # v0_8_15 = 815 # No Examples
    v0_8_16 = 816
    v0_8_17 = 817
    # v0_8_18 = 818 # Not Included
    v0_8_19 = 819


# v0_8_20 = 820 # No Examples
# v0_9_0 = 90 # No Examples
# v0_9_1 = 91 # No Examples
# v0_9_2 = 92 # No Examples
# v0_9_3 = 93 # No Examples
# v0_9_4 = 94 # No Examples
# v1_0_0 = 10 # No Examples


"""

Unaccounted for:

    General
         - New Types that are not necessary for a compliant new schema version
         - Changing Namespace key value

    17 -> 19
        New Namespaces - Line 6
        New Indexes - Line 177
            <xsd:key name="ChildRefKey">
                <xsd:selector xpath=".//mdl:Module | .//mdl:SubModule | .//mdl:DeviceModule | .//mdl:DeviceSubModule "/>
                <xsd:field xpath="@ID"/>
            </xsd:key>
            <xsd:keyref name="ChildRefKeyRef" refer="mdl:ChildRefKey">
                <xsd:selector xpath=".//mdl:ChildRef"/>
                <xsd:field xpath="@IDREF"/>
            </xsd:keyref>

    16 -> 17
        Splitting a parameter into two different types
            <xsd:element name="SignalRange" type="ConditionParametersType"/>
                to
            <xsd:element name="EUSignalRange" type="ConditionParametersType" minOccurs="0"/>
            <xsd:element name="IUSignalRange" type="ConditionParametersType" minOccurs="0"/>
        
    


Test values
"""


class XmlElement(Enum):
    WrapInChoiceWithNewElementType = (
        MdlVersion.v0_8_16, MdlVersion.v0_8_17, 5276,
        'Encapsulate an existing element type in a choice with a new type',

        """
    <xsd:complexType name="RouteType">
        <xsd:annotation>
            <xsd:documentation>
                A Route element describes a single network route configured on a network interface of the NetworkNode.
            </xsd:documentation>
            <xsd:documentation>
                A Destination element, of type mdl:IPAddress, describes the destination network of the route.
            </xsd:documentation>
            <xsd:documentation>
                A Netmask element, of type mdl:IPAddress, describes the netmask of the route.
            </xsd:documentation>
            <xsd:documentation>
                A Gateway element, of type mdl:IPAddress, describes the gateway address for the route.
            </xsd:documentation>
            <xsd:documentation>
                A Metric element, of type xsd:positiveInteger, describes the metric of the route.
            </xsd:documentation>
            <xsd:documentation>
                A NetworkInterfaceRef identifies the NetworkInterface element to which the route applies.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
            <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
            <xsd:element name="Destination" type="IPAddress"/>
            <xsd:element name="Netmask" type="IPAddress"/>
            <xsd:element name="Gateway" type="IPAddress"/>
            <xsd:element name="Metric" type="xsd:positiveInteger"/>
            <xsd:element name="NetworkInterfaceRef" type="NetworkInterfaceRefType"/>
        </xsd:sequence>
    </xsd:complexType>
    <xsd:simpleType name="IPAddress">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPAddress data type is an xsd:union of the mdl:IPv4Address and mdl:IPv6Address data types.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:union memberTypes="IPv4Address IPv6Address"/>
    </xsd:simpleType>
    <xsd:simpleType name="IPv4Address">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPv4Address data type is an xsd:string of decimal digits separated by '.' (period) characters. It represents the 32-bit dot-decimal notation of IPv4 addresses. Valid IPv4 addresses contain a series of four one-byte long decimal numbers (0 - 255) separated by the '.' character (a total of three '.' characters appear). A valid value of the mdl:IPv4Address data type is "212.23.123.0". See RFC 790 in Section 2.2.2 for further details on IPv4 addresses.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern
                    value="(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"/>
        </xsd:restriction>
    </xsd:simpleType>
    <xsd:simpleType name="IPv6Address">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPv6Address data type is an xsd:string of hexadecimal digits ('0'-'9' and 'A'-'F' or 'a'-'f') separated by ':' (colon) characters. It represents the 128-bit notation of IPv6 addresses. Fully expanded IPv6 addresses contain a series of eight two-byte long hexadecimal numbers separated by the ":" character (a total of seven ':' characters appear). A single two-byte long hexadecimal number contains up to four hexadecimal digits. All valid IPv6 addresses are supported in the MDL. A valid value of the mdl:IPv6Address data type is "2001:DB8:0000:0056:0000:ABCD:EF12:1234". See RFC 2460 for further details on IPv6 addresses and valid shorthand notations.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern
                    value="((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4})|:))|(([0-9A-Fa-f]{1,4}:){6}(:|((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})|(:[0-9A-Fa-f]{1,4})))|(([0-9A-Fa-f]{1,4}:){5}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:)(:[0-9A-Fa-f]{1,4}){0,4}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(:(:[0-9A-Fa-f]{1,4}){0,5}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})))(%.+)?"/>
        </xsd:restriction>
    </xsd:simpleType>
    <xsd:complexType name="NetworkInterfaceRefType">
        <xsd:annotation>
            <xsd:documentation>
                The value of the IDREF of a NetworkInterfaceRef shall refer to the ID attribute of a NetworkInterface within the same NetworkNode.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="IDREF" type="xsd:IDREF" use="required"/>
    </xsd:complexType>
    <xsd:complexType name="RadioLinkRefType">
        <xsd:annotation>
            <xsd:documentation>
                The value of the IDREF of a RadioLinkRef must refer to the ID attribute of a RadioLinkType.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="IDREF" type="xsd:IDREF" use="required"/>
    </xsd:complexType>
    <xsd:complexType name="NetworkInterfaceType">
        <xsd:annotation>
            <xsd:documentation>
                A NetworkInterface element describes the module or component that provides the interface between a NetworkNode and a network. A NetworkNode can contain one or more NetworkInterfaces, and will have a NetworkInterface element describing each. This element and all sub-elements are conditional as described in the MDLRoot documentation. Components that require and shall process this element for configuration are:
            </xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="Name" type="xsd:token"/>
        </xsd:sequence>
        <xsd:attribute name="ID" type="xsd:ID" use="required"/>
    </xsd:complexType>
        """,
        """
    <xsd:element name="Route" type="RouteType"/>
    <xsd:element name="NetworkInterface" type="NetworkInterfaceType"/>
        """,

        """
    <xsd:complexType name="RouteType">
        <xsd:annotation>
            <xsd:documentation>
                A Route element describes a single network route configured on a network interface of the NetworkNode.
            </xsd:documentation>
            <xsd:documentation>
                A Destination element, of type mdl:IPAddress, describes the destination network of the route.
            </xsd:documentation>
            <xsd:documentation>
                A Netmask element, of type mdl:IPAddress, describes the netmask of the route.
            </xsd:documentation>
            <xsd:documentation>
                A Gateway element, of type mdl:IPAddress, describes the gateway address for the route.
            </xsd:documentation>
            <xsd:documentation>
                A Metric element, of type xsd:positiveInteger, describes the metric of the route.
            </xsd:documentation>
            <xsd:documentation>
                The NetworkInterfaceRef or RadioLinkRef identifies the network interface, either wired or wireless, to which the route applies.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
            <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
            <xsd:element name="Destination" type="IPAddress"/>
            <xsd:element name="Netmask" type="IPAddress"/>
            <xsd:element name="Gateway" type="IPAddress" minOccurs="0"/>
            <xsd:element name="Metric" type="xsd:positiveInteger" minOccurs="0"/>
            <xsd:choice>
                <xsd:element name="NetworkInterfaceRef" type="NetworkInterfaceRefType"/>
                <xsd:element name="RadioLinkRef" type="RadioLinkRefType"/>
            </xsd:choice>
        </xsd:sequence>
    </xsd:complexType>
    <xsd:simpleType name="IPAddress">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPAddress data type is an xsd:union of the mdl:IPv4Address and mdl:IPv6Address data types.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:union memberTypes="IPv4Address IPv6Address"/>
    </xsd:simpleType>

    <xsd:simpleType name="IPv4Address">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPv4Address data type is an xsd:string of decimal digits separated by '.' (period) characters. It represents the 32-bit dot-decimal notation of IPv4 addresses. Valid IPv4 addresses contain a series of four one-byte long decimal numbers (0 - 255) separated by the '.' character (a total of three '.' characters appear). A valid value of the mdl:IPv4Address data type is "212.23.123.0". See RFC 790 in Section 2.2.2 for further details on IPv4 addresses.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern
                    value="(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"/>
        </xsd:restriction>
    </xsd:simpleType>
    <xsd:simpleType name="IPv6Address">
        <xsd:annotation>
            <xsd:documentation>
                The mdl:IPv6Address data type is an xsd:string of hexadecimal digits ('0'-'9' and 'A'-'F' or 'a'-'f') separated by ':' (colon) characters. It represents the 128-bit notation of IPv6 addresses. Fully expanded IPv6 addresses contain a series of eight two-byte long hexadecimal numbers separated by the ":" character (a total of seven ':' characters appear). A single two-byte long hexadecimal number contains up to four hexadecimal digits. All valid IPv6 addresses are supported in the MDL. A valid value of the mdl:IPv6Address data type is "2001:DB8:0000:0056:0000:ABCD:EF12:1234". See RFC 2460 for further details on IPv6 addresses and valid shorthand notations.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:string">
            <xsd:pattern
                    value="((([0-9A-Fa-f]{1,4}:){7}(([0-9A-Fa-f]{1,4})|:))|(([0-9A-Fa-f]{1,4}:){6}(:|((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})|(:[0-9A-Fa-f]{1,4})))|(([0-9A-Fa-f]{1,4}:){5}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(([0-9A-Fa-f]{1,4}:)(:[0-9A-Fa-f]{1,4}){0,4}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(:(:[0-9A-Fa-f]{1,4}){0,5}((:((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})?)|((:[0-9A-Fa-f]{1,4}){1,2})))|(((25[0-5]|2[0-4]\d|[01]?\d{1,2})(\.(25[0-5]|2[0-4]\d|[01]?\d{1,2})){3})))(%.+)?"/>
        </xsd:restriction>
    </xsd:simpleType>
    <xsd:complexType name="NetworkInterfaceRefType">
        <xsd:annotation>
            <xsd:documentation>
                The value of the IDREF of a NetworkInterfaceRef shall refer to the ID attribute of a NetworkInterface within the same NetworkNode.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="IDREF" type="xsd:IDREF" use="required"/>
    </xsd:complexType>
    <xsd:complexType name="NetworkInterfaceType">
        <xsd:annotation>
            <xsd:documentation>
                A NetworkInterface element describes the module or component
            </xsd:documentation>
        </xsd:annotation>
        <xsd:sequence>
            <xsd:element name="Name" type="xsd:token"/>
            <xsd:element name="Description" type="xsd:string" minOccurs="0"/>
            <xsd:element name="IPAddress" type="IPAddress" minOccurs="0"/>
        </xsd:sequence>
        <xsd:attribute name="ID" type="xsd:ID" use="required"/>
    </xsd:complexType>
    <xsd:complexType name="RadioLinkRefType">
        <xsd:annotation>
            <xsd:documentation>
                The value of the IDREF of a RadioLinkRef must refer to the ID attribute of a RadioLinkType.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="IDREF" type="xsd:IDREF" use="required"/>
    </xsd:complexType>
        """,
        """
    <xsd:element name="Route" type="RouteType"/>
    <xsd:element name="NetworkInterface" type="NetworkInterfaceType"/>
        """,
        {'Default': """
    <Route>
        <Destination>192.168.1.55</Destination>
        <Netmask>255.255.255.0</Netmask>
        <Gateway>192.168.1.1</Gateway>
        <Metric>1</Metric>
        <NetworkInterfaceRef IDREF="MyId"/>
    </Route>
    <NetworkInterface ID="MyId">
        <Name>MyNetworkInterface</Name>
    </NetworkInterface>
    """})

    PositiveToNonNegativeInteger = (
        MdlVersion.v0_8_16, MdlVersion.v0_8_17, 4138, 'Change Integer from positive to non-null',
        """
          <xsd:complexType name="SubModuleType">
    <xsd:annotation>
      <xsd:documentation>
        The SubModule element describes the properties of sub-module or card in a DAU.
      </xsd:documentation>
    </xsd:annotation>
    <xsd:sequence>
      <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
      <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
      <xsd:element name="Name" type="xsd:token"/>
      <xsd:element name="Manufacturer" type="xsd:string"/>
      <xsd:element name="ModelNumber" type="xsd:string"/>
      <xsd:element name="SerialNumber" type="xsd:string"/>
      <xsd:element name="Position" type="xsd:positiveInteger"/>
      <xsd:element name="Connector" type="ConnectorType" minOccurs="0" maxOccurs="unbounded"/>
    </xsd:sequence>
  </xsd:complexType>
          <xsd:complexType name="ConnectorType">
    <xsd:annotation>
      <xsd:documentation>
        The Connector element describes a physical connector on a NetworkNode or a module and contains one or more Pin elements.
        This element and sub-elements are conditional as described in the MDLRoot documentation.  Components that require and shall 
        process this element for configuration are: 
        DAU
      </xsd:documentation>
    </xsd:annotation>
    <xsd:sequence>
      <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
      <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
      <xsd:element name="Name" type="xsd:token"/>
      <xsd:element name="Description" type="xsd:string"/>
    </xsd:sequence>
  </xsd:complexType>
        """,
        '<xsd:element name="SubModule" type="SubModuleType"/>',

        """
          <xsd:complexType name="SubModuleType">
    <xsd:annotation>
      <xsd:documentation>
        The SubModule element describes the properties of sub-module or card in a DAU.
      </xsd:documentation>
    </xsd:annotation>
    <xsd:sequence>
      <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
      <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
      <xsd:element name="Name" type="xsd:token"/>
      <xsd:element name="Manufacturer" type="xsd:string"/>
      <xsd:element name="ModelNumber" type="xsd:string"/>
      <xsd:element name="SerialNumber" type="xsd:string"/>
      <xsd:element name="Position" type="xsd:nonNegativeInteger"/>
      <xsd:element name="Connector" type="ConnectorType" minOccurs="0" maxOccurs="unbounded"/>
    </xsd:sequence>
  </xsd:complexType>
          <xsd:complexType name="ConnectorType">
    <xsd:annotation>
      <xsd:documentation>
        The Connector element describes a physical connector on a NetworkNode or a module and contains one or more Pin elements.
        This element and sub-elements are conditional as described in the MDLRoot documentation.  Components that require and shall 
        process this element for configuration are: 
        DAU
      </xsd:documentation>
    </xsd:annotation>
    <xsd:sequence>
      <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
      <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
      <xsd:element name="Name" type="xsd:token"/>
      <xsd:element name="Description" type="xsd:string"/>
    </xsd:sequence>
  </xsd:complexType>
        """,
        '<xsd:element name="SubModule" type="SubModuleType"/>',

        {'Default': """
                         <SubModule>
                     <Name>Submodule</Name>
                     <Manufacturer>Misc Manufacturer</Manufacturer>
                     <ModelNumber>007</ModelNumber>
                     <SerialNumber>24895t7234598</SerialNumber>
                     <Position>1</Position>
                 </SubModule>
                     """})

    BooleanElementRemoval = (
        MdlVersion.v0_8_17, MdlVersion.v0_8_19, 781, 'Removal of a single boolean element',
        """
<xsd:complexType name="RadioLinkType">
<xsd:annotation>
<xsd:documentation>
The RadioLink element describes a one-way connection between
two radio endpoints.
</xsd:documentation>
</xsd:annotation>
<xsd:sequence>
<xsd:element name="Description" type="xsd:string"/>
<xsd:element name="TxRxEnable" type="xsd:boolean"/>
<xsd:element name="LinkControlMode" type="xsd:boolean"/>
<xsd:element name="EncryptionKeyID" type="xsd:unsignedInt"/>
<xsd:element name="ARQEnableFlag" type="xsd:boolean"/>
</xsd:sequence>
</xsd:complexType>
        """,
        '<xsd:element name="RadioLink" type="RadioLinkType"/>',

        """
<xsd:complexType name="RadioLinkType">
<xsd:annotation>
<xsd:documentation>
The RadioLink element describes a one-way connection between
two radio endpoints.
</xsd:documentation>
</xsd:annotation>
<xsd:sequence>
<xsd:element name="Description" type="xsd:string"/>
<xsd:element name="TxRxEnable" type="xsd:boolean"/>
<xsd:element name="EncryptionKeyID" type="xsd:unsignedInt"/>
<xsd:element name="ARQEnableFlag" type="xsd:boolean"/>
</xsd:sequence>
</xsd:complexType>
""",
        '<xsd:element name="RadioLink" type="RadioLinkType"/>',
        {'Default': """
<RadioLink>
<Description>This is a radio link</Description>
<TxRxEnable>true</TxRxEnable>
<LinkControlMode>true</LinkControlMode>
<EncryptionKeyID>567</EncryptionKeyID>
<ARQEnableFlag>false</ARQEnableFlag>

</RadioLink>
        """})

    ExtendedEnum = (
        MdlVersion.v0_8_17, MdlVersion.v0_8_19, 7761,
        'The Enum has been replaced with an element that extends it to provide additional data',
        """
          <xsd:simpleType name="MeasurementTypeEnum">
<xsd:annotation>
<xsd:documentation>
The MeasurementType element is an enumeration of type
mdl:MeasurementTypeEnum that can be set to one of the following
values: "Analog", "Discrete", "DigitalBus", "Computed"
</xsd:documentation><xsd:documentation>
If "Analog" or "Discrete" is selected, the AnalogAttributes
element shall be used to describe the Measurement element.  If
"DigitalBus" or "Computed" is selected, the DigitalAttributes element
shall be used to describe the Measurement element.
</xsd:documentation>
</xsd:annotation>
<xsd:restriction base="xsd:string">
<xsd:enumeration value="Analog"/>
<xsd:enumeration value="Discrete"/>
<xsd:enumeration value="DigitalBus"/>
<xsd:enumeration value="Computed"/>
</xsd:restriction>
</xsd:simpleType>
        """,
        '<xsd:element name="MeasurementType" type="MeasurementTypeEnum"/>',

        """
<xsd:complexType name="MeasurementTypeEnumExtType">
<xsd:annotation>
<xsd:documentation>
The MeasurementTypeEnumExtType provides an extensible enumeration.  If the
value of the enumeration is "Extension", the attribute named
"extension" shall contain the string representing the extended
enumeration value.
</xsd:documentation>
</xsd:annotation>
<xsd:simpleContent>
<xsd:extension base="MeasurementTypeEnum">
<xsd:attribute name="extension" type="xsd:string"/>
</xsd:extension>
</xsd:simpleContent>
</xsd:complexType>	

<xsd:simpleType name="MeasurementTypeEnum">
<xsd:annotation>
<xsd:documentation>
The MeasurementType element is an enumeration that describes the 
basic kind of measurement.  Some example values are Analog, Discrete,
DigitalBus, Computed, Time, Video, etc.
</xsd:documentation><xsd:documentation>
If "Analog" or "Discrete" is selected, the AnalogAttributes
element shall be used to describe the Measurement element.  If
"DigitalBus" or "Computed" is selected, the DigitalAttributes element
shall be used to describe the Measurement element.
</xsd:documentation><xsd:documentation>
If "Time" is selected, the TimeAttributes element shall be used
to describe the Measurement element.
</xsd:documentation>
</xsd:annotation>
<xsd:restriction base="xsd:string">
<xsd:enumeration value="Analog"/>
<xsd:enumeration value="Discrete"/>
<xsd:enumeration value="DigitalBus"/>
<xsd:enumeration value="Computed"/>
<xsd:enumeration value="Time"/>
<xsd:enumeration value="Video"/>
<xsd:enumeration value="Overhead"/>
<xsd:enumeration value="Extension"/>
</xsd:restriction>
</xsd:simpleType>
""",
        '<xsd:element name="MeasurementType" type="MeasurementTypeEnumExtType"/>',
        {'Default': '<MeasurementType>DigitalBus</MeasurementType>'})

    MultipleToNestedMultiple = (
        MdlVersion.UNKNOWN, MdlVersion.UNKNOWN, -1,
        'A single "NameValue" object restructured to be within a "NameValues" object to allow multiples',

        """
          <xsd:complexType name="NameValueType">
    <xsd:simpleContent>
      <xsd:annotation>
        <xsd:documentation>
          The NameValue element is a Name/Value pair used to document
          decisions made to arrive at a vendor-specific configuration.  The
          Index attribute may be used to document the ordering of sibling
          NameValue elements.
        </xsd:documentation>
      </xsd:annotation>
      <xsd:extension base="xsd:string">
        <xsd:attribute name="Name" type="xsd:token" use="required"/>
        <xsd:attribute name="Index" type="xsd:positiveInteger" use="required"/>
      </xsd:extension>
    </xsd:simpleContent>
  </xsd:complexType>
  """,

        """<xsd:element name="NameValue" type="NameValueType" maxOccurs="unbounded"/>""",

        """
          <xsd:complexType name="NameValuesType">
    <xsd:annotation>
      <xsd:documentation>
        The NameValues element is a container for zero or more
        TmNSManageableApp elements.
      </xsd:documentation>
    </xsd:annotation>
    <xsd:sequence>
      <xsd:element name="ReadOnly" type="xsd:boolean" default="false" minOccurs="0"/>
      <xsd:element name="Owner" type="xsd:token" minOccurs="0"/>
      <xsd:element name="NameValue" type="NameValueType" maxOccurs="unbounded"/>
    </xsd:sequence>
  </xsd:complexType>
  <xsd:complexType name="NameValueType">
    <xsd:simpleContent>
      <xsd:annotation>
        <xsd:documentation>
          The NameValue element is a Name/Value pair used to document
          decisions made to arrive at a vendor-specific configuration.  The
          Index attribute may be used to document the ordering of sibling
          NameValue elements.
        </xsd:documentation>
      </xsd:annotation>
      <xsd:extension base="xsd:string">
        <xsd:attribute name="Name" type="xsd:token" use="required"/>
        <xsd:attribute name="Index" type="xsd:positiveInteger" use="required"/>
      </xsd:extension>
    </xsd:simpleContent>
  </xsd:complexType>
  """,

        """<xsd:element name="NameValues" type="NameValuesType"/>""",

        {
            "OneValue": """<NameValue Name="Test0" Index="1">One</NameValue>""",
            "ThreeValues": """<NameValue Name="Test0" Index="1">One</NameValue>
            <NameValue Name="Test1" Index="2">Two</NameValue>
            <NameValue Name="Test2" Index="3">Three</NameValue>"""
        }
    )

    def __init__(self, initial_version: MdlVersion, updated_version: MdlVersion, initial_deviation_line: int,
                 description: str,
                 xsd_initial_declaration: str,
                 xsd_initial_usage: str,
                 xsd_updated_declaration: str,
                 xsd_updated_usage: str,
                 xml_initial_map: Dict[str, str],
                 ):
        self.initial_version = initial_version
        self.updated_version = updated_version
        self.initial_deviation_line = initial_deviation_line
        self.description = description
        self.xsd_initial_declaration = xsd_initial_declaration
        self.xsd_updated_declaration = xsd_updated_declaration
        self.xsd_initial_usage = xsd_initial_usage
        self.xsd_updated_usage = xsd_updated_usage
        self.xml_initial_map = xml_initial_map
        self._parent_path = None
        self._xml_path = None

    @property
    def root_path(self):
        if self._parent_path is None:
            self._parent_path = os.path.join(config.build_path, self.name)
            if not os.path.exists(self._parent_path):
                os.mkdir(self._parent_path)
        return self._parent_path

    @property
    def xml_path(self):
        if self._xml_path is None:
            self._xml_path = os.path.join(self.root_path, 'xml')
            if not os.path.exists(self._xml_path):
                os.mkdir(self._xml_path)
        return self._xml_path

    @property
    def initial_xsd_path(self):
        return os.path.abspath(os.path.join(self.root_path, 'initial.xsd'))

    @property
    def updated_xsd_path(self):
        return os.path.abspath(os.path.join(self.root_path, 'updated.xsd'))

    def get_initial_xml_path(self, key: str):
        return os.path.abspath(os.path.join(self.xml_path, 'initial-' + key + '.xml'))

    @property
    def initial_xml_paths(self) -> List[str]:
        return list(map(lambda x: self.get_initial_xml_path(x), self.xml_initial_map.keys()))
