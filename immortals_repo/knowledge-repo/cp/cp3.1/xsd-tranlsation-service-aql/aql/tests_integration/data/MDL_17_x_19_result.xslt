<?xml version='1.0' encoding='utf-8'?>
<xsl:stylesheet xmlns:mdl="http://inetprogram.org/projects/MDL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="1.0">
  <!-- identity template, copies everything as is -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/N2NPortMapping/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/N2NPortMapping/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/N2NPortMapping/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/VendorConfig/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/VendorConfig">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/VendorConfig/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/PhysicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/PhysicalLocation/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/PhysicalLocation/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/Device/DeviceStructure" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device">
    <DeviceStructure>
      <Description/>
      <DeviceModule>
        <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/Calibration"/>
        <Children>
          <Child>
            <ChildRef/>
            <Description/>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
          </Child>
          <Owner/>
          <ReadOnly/>
        </Children>
        <Connector>
          <Description/>
          <Name/>
          <Owner/>
          <Pin>
            <DataOperationRefs>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/DataOperationRef"/>
              <Owner/>
              <ReadOnly/>
            </DataOperationRefs>
            <Description/>
            <Designator/>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
            <Owner/>
            <ReadOnly/>
            <VendorConfig>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </VendorConfig>
          </Pin>
          <ReadOnly/>
        </Connector>
        <DataOperationRef/>
        <Description/>
        <DeviceSubModule>
          <Children>
            <Child>
              <ChildRef/>
              <Description/>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
            </Child>
            <Owner/>
            <ReadOnly/>
          </Children>
          <Connector>
            <Description/>
            <Name/>
            <Owner/>
            <Pin>
              <DataOperationRefs>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/DataOperationRef"/>
                <Owner/>
                <ReadOnly/>
              </DataOperationRefs>
              <Description/>
              <Designator/>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
              <Owner/>
              <ReadOnly/>
              <VendorConfig>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </VendorConfig>
            </Pin>
            <ReadOnly/>
          </Connector>
          <Description/>
          <GenericParameter>
            <NameValues>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
              <Owner/>
              <ReadOnly/>
            </NameValues>
            <Owner/>
            <ReadOnly/>
          </GenericParameter>
          <InventoryID/>
          <Manufacturer/>
          <Model/>
          <Name/>
          <Owner/>
          <Ports>
            <Owner/>
            <Port>
              <AnalogAttributes>
                <EUSignalRange>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </EUSignalRange>
                <FrequencyContent>
                  <FrequencyBand>
                    <Attenuation>
                      <ConditionParameter>
                        <ConditionOperation/>
                        <ConditionValueFlex/>
                        <ConditionValueFloat/>
                        <Owner/>
                        <ReadOnly/>
                        <SIUnits/>
                        <UnitsRef/>
                      </ConditionParameter>
                      <Owner/>
                      <ReadOnly/>
                    </Attenuation>
                    <BandType/>
                    <EdgeFrequency>
                      <ConditionParameter>
                        <ConditionOperation/>
                        <ConditionValueFlex/>
                        <ConditionValueFloat/>
                        <Owner/>
                        <ReadOnly/>
                        <SIUnits/>
                        <UnitsRef/>
                      </ConditionParameter>
                      <Owner/>
                      <ReadOnly/>
                    </EdgeFrequency>
                    <Owner/>
                    <ReadOnly/>
                  </FrequencyBand>
                  <Owner/>
                  <PercentSignal/>
                  <ReadOnly/>
                </FrequencyContent>
                <IUSignalRange>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </IUSignalRange>
                <Owner/>
                <ReadOnly/>
                <Resolution>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </Resolution>
              </AnalogAttributes>
              <Description/>
              <Excitation>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </Excitation>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
              <Owner/>
              <PhysicalNetworkPortRef/>
              <PinRef/>
              <PortDirection/>
              <PortType/>
              <ReadOnly/>
              <VendorConfig>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </VendorConfig>
            </Port>
            <ReadOnly/>
          </Ports>
          <Position/>
          <PositionsOccupied/>
          <ReadOnly/>
          <SerialIdentifier/>
          <VendorConfig>
            <NameValues>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
              <Owner/>
              <ReadOnly/>
            </NameValues>
            <Owner/>
            <ReadOnly/>
          </VendorConfig>
        </DeviceSubModule>
        <Excitation>
          <ConditionParameter>
            <ConditionOperation/>
            <ConditionValueFlex/>
            <ConditionValueFloat/>
            <Owner/>
            <ReadOnly/>
            <SIUnits/>
            <UnitsRef/>
          </ConditionParameter>
          <Owner/>
          <ReadOnly/>
        </Excitation>
        <GenericParameter>
          <NameValues>
            <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
            <Owner/>
            <ReadOnly/>
          </NameValues>
          <Owner/>
          <ReadOnly/>
        </GenericParameter>
        <InventoryID/>
        <Manufacturer/>
        <Model/>
        <Name/>
        <Owner/>
        <Ports>
          <Owner/>
          <Port>
            <AnalogAttributes>
              <EUSignalRange>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </EUSignalRange>
              <FrequencyContent>
                <FrequencyBand>
                  <Attenuation>
                    <ConditionParameter>
                      <ConditionOperation/>
                      <ConditionValueFlex/>
                      <ConditionValueFloat/>
                      <Owner/>
                      <ReadOnly/>
                      <SIUnits/>
                      <UnitsRef/>
                    </ConditionParameter>
                    <Owner/>
                    <ReadOnly/>
                  </Attenuation>
                  <BandType/>
                  <EdgeFrequency>
                    <ConditionParameter>
                      <ConditionOperation/>
                      <ConditionValueFlex/>
                      <ConditionValueFloat/>
                      <Owner/>
                      <ReadOnly/>
                      <SIUnits/>
                      <UnitsRef/>
                    </ConditionParameter>
                    <Owner/>
                    <ReadOnly/>
                  </EdgeFrequency>
                  <Owner/>
                  <ReadOnly/>
                </FrequencyBand>
                <Owner/>
                <PercentSignal/>
                <ReadOnly/>
              </FrequencyContent>
              <IUSignalRange>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </IUSignalRange>
              <Owner/>
              <ReadOnly/>
              <Resolution>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </Resolution>
            </AnalogAttributes>
            <Description/>
            <Excitation>
              <ConditionParameter>
                <ConditionOperation/>
                <ConditionValueFlex/>
                <ConditionValueFloat/>
                <Owner/>
                <ReadOnly/>
                <SIUnits/>
                <UnitsRef/>
              </ConditionParameter>
              <Owner/>
              <ReadOnly/>
            </Excitation>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
            <Owner/>
            <PhysicalNetworkPortRef/>
            <PinRef/>
            <PortDirection/>
            <PortType/>
            <ReadOnly/>
            <VendorConfig>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </VendorConfig>
          </Port>
          <ReadOnly/>
        </Ports>
        <Position/>
        <PositionsOccupied/>
        <ReadOnly/>
        <Sensitivity>
          <ConditionParameter>
            <ConditionOperation/>
            <ConditionValueFlex/>
            <ConditionValueFloat/>
            <Owner/>
            <ReadOnly/>
            <SIUnits/>
            <UnitsRef/>
          </ConditionParameter>
          <Owner/>
          <ReadOnly/>
        </Sensitivity>
        <SerialIdentifier/>
        <VendorConfig>
          <NameValues>
            <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
            <Owner/>
            <ReadOnly/>
          </NameValues>
          <Owner/>
          <ReadOnly/>
        </VendorConfig>
      </DeviceModule>
      <GenericParameter>
        <NameValues>
          <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/LogicalLocation/GenericParameter/NameValue"/>
          <Owner/>
          <ReadOnly/>
        </NameValues>
        <Owner/>
        <ReadOnly/>
      </GenericParameter>
      <Name/>
      <Owner/>
      <ReadOnly/>
    </DeviceStructure>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/PortMappings/PortMapping/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/PortMappings/PortMapping/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/PortMappings/PortMapping/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/VendorConfig/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/VendorConfig">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/VendorConfig/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/VendorConfig/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/VendorConfig">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/VendorConfig/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/PhysicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/PhysicalLocation/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/PhysicalLocation/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/NetworkDomains/Network/NetworkNode/InternalStructure" -->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode">
    <InternalStructure>
      <Description/>
      <GenericParameter>
        <NameValues>
          <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
          <Owner/>
          <ReadOnly/>
        </NameValues>
        <Owner/>
        <ReadOnly/>
      </GenericParameter>
      <Module>
        <Children>
          <Child>
            <ChildRef/>
            <Description/>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
          </Child>
          <Owner/>
          <ReadOnly/>
        </Children>
        <Connector>
          <Description/>
          <Name/>
          <Owner/>
          <Pin>
            <DataOperationRefs>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/DataOperationRef"/>
              <Owner/>
              <ReadOnly/>
            </DataOperationRefs>
            <Description/>
            <Designator/>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
            <Owner/>
            <ReadOnly/>
            <VendorConfig>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </VendorConfig>
          </Pin>
          <ReadOnly/>
        </Connector>
        <Description/>
        <GenericParameter>
          <NameValues>
            <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
            <Owner/>
            <ReadOnly/>
          </NameValues>
          <Owner/>
          <ReadOnly/>
        </GenericParameter>
        <InventoryID/>
        <Manufacturer/>
        <Model/>
        <Name/>
        <NetworkInterface>
          <DHCPEnable/>
          <Description/>
          <Gateway/>
          <GenericParameter>
            <NameValues>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
              <Owner/>
              <ReadOnly/>
            </NameValues>
            <Owner/>
            <ReadOnly/>
          </GenericParameter>
          <IPAddress/>
          <MACAddress/>
          <Name/>
          <Netmask/>
          <Owner/>
          <PhysicalNetworkPort>
            <Description/>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <IEEE1588VersionOfPort/>
            <Medium/>
            <Name/>
            <Owner/>
            <PortDataRate>
              <FloatValue/>
              <Owner/>
              <ReadOnly/>
              <SIUnits/>
              <UnitsRef/>
            </PortDataRate>
            <PortNumber/>
            <ReadOnly/>
          </PhysicalNetworkPort>
          <ReadOnly/>
        </NetworkInterface>
        <Owner/>
        <Ports>
          <Owner/>
          <Port>
            <AnalogAttributes>
              <EUSignalRange>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </EUSignalRange>
              <FrequencyContent>
                <FrequencyBand>
                  <Attenuation>
                    <ConditionParameter>
                      <ConditionOperation/>
                      <ConditionValueFlex/>
                      <ConditionValueFloat/>
                      <Owner/>
                      <ReadOnly/>
                      <SIUnits/>
                      <UnitsRef/>
                    </ConditionParameter>
                    <Owner/>
                    <ReadOnly/>
                  </Attenuation>
                  <BandType/>
                  <EdgeFrequency>
                    <ConditionParameter>
                      <ConditionOperation/>
                      <ConditionValueFlex/>
                      <ConditionValueFloat/>
                      <Owner/>
                      <ReadOnly/>
                      <SIUnits/>
                      <UnitsRef/>
                    </ConditionParameter>
                    <Owner/>
                    <ReadOnly/>
                  </EdgeFrequency>
                  <Owner/>
                  <ReadOnly/>
                </FrequencyBand>
                <Owner/>
                <PercentSignal/>
                <ReadOnly/>
              </FrequencyContent>
              <IUSignalRange>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </IUSignalRange>
              <Owner/>
              <ReadOnly/>
              <Resolution>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </Resolution>
            </AnalogAttributes>
            <Description/>
            <Excitation>
              <ConditionParameter>
                <ConditionOperation/>
                <ConditionValueFlex/>
                <ConditionValueFloat/>
                <Owner/>
                <ReadOnly/>
                <SIUnits/>
                <UnitsRef/>
              </ConditionParameter>
              <Owner/>
              <ReadOnly/>
            </Excitation>
            <GenericParameter>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </GenericParameter>
            <Name/>
            <Owner/>
            <PhysicalNetworkPortRef/>
            <PinRef/>
            <PortDirection/>
            <PortType/>
            <ReadOnly/>
            <VendorConfig>
              <NameValues>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                <Owner/>
                <ReadOnly/>
              </NameValues>
              <Owner/>
              <ReadOnly/>
            </VendorConfig>
          </Port>
          <ReadOnly/>
        </Ports>
        <Position/>
        <PositionsOccupied/>
        <ReadOnly/>
        <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/Routes"/>
        <RunningApps>
          <ManageableAppRef/>
          <Owner/>
          <ReadOnly/>
        </RunningApps>
        <SerialIdentifier/>
        <SubModule>
          <Children>
            <Child>
              <ChildRef/>
              <Description/>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
            </Child>
            <Owner/>
            <ReadOnly/>
          </Children>
          <Connector>
            <Description/>
            <Name/>
            <Owner/>
            <Pin>
              <DataOperationRefs>
                <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/Device/DataOperationRef"/>
                <Owner/>
                <ReadOnly/>
              </DataOperationRefs>
              <Description/>
              <Designator/>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
              <Owner/>
              <ReadOnly/>
              <VendorConfig>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </VendorConfig>
            </Pin>
            <ReadOnly/>
          </Connector>
          <Description/>
          <GenericParameter>
            <NameValues>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
              <Owner/>
              <ReadOnly/>
            </NameValues>
            <Owner/>
            <ReadOnly/>
          </GenericParameter>
          <InventoryID/>
          <Manufacturer/>
          <Model/>
          <Name/>
          <Owner/>
          <Ports>
            <Owner/>
            <Port>
              <AnalogAttributes>
                <EUSignalRange>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </EUSignalRange>
                <FrequencyContent>
                  <FrequencyBand>
                    <Attenuation>
                      <ConditionParameter>
                        <ConditionOperation/>
                        <ConditionValueFlex/>
                        <ConditionValueFloat/>
                        <Owner/>
                        <ReadOnly/>
                        <SIUnits/>
                        <UnitsRef/>
                      </ConditionParameter>
                      <Owner/>
                      <ReadOnly/>
                    </Attenuation>
                    <BandType/>
                    <EdgeFrequency>
                      <ConditionParameter>
                        <ConditionOperation/>
                        <ConditionValueFlex/>
                        <ConditionValueFloat/>
                        <Owner/>
                        <ReadOnly/>
                        <SIUnits/>
                        <UnitsRef/>
                      </ConditionParameter>
                      <Owner/>
                      <ReadOnly/>
                    </EdgeFrequency>
                    <Owner/>
                    <ReadOnly/>
                  </FrequencyBand>
                  <Owner/>
                  <PercentSignal/>
                  <ReadOnly/>
                </FrequencyContent>
                <IUSignalRange>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </IUSignalRange>
                <Owner/>
                <ReadOnly/>
                <Resolution>
                  <ConditionParameter>
                    <ConditionOperation/>
                    <ConditionValueFlex/>
                    <ConditionValueFloat/>
                    <Owner/>
                    <ReadOnly/>
                    <SIUnits/>
                    <UnitsRef/>
                  </ConditionParameter>
                  <Owner/>
                  <ReadOnly/>
                </Resolution>
              </AnalogAttributes>
              <Description/>
              <Excitation>
                <ConditionParameter>
                  <ConditionOperation/>
                  <ConditionValueFlex/>
                  <ConditionValueFloat/>
                  <Owner/>
                  <ReadOnly/>
                  <SIUnits/>
                  <UnitsRef/>
                </ConditionParameter>
                <Owner/>
                <ReadOnly/>
              </Excitation>
              <GenericParameter>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </GenericParameter>
              <Name/>
              <Owner/>
              <PhysicalNetworkPortRef/>
              <PinRef/>
              <PortDirection/>
              <PortType/>
              <ReadOnly/>
              <VendorConfig>
                <NameValues>
                  <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
                  <Owner/>
                  <ReadOnly/>
                </NameValues>
                <Owner/>
                <ReadOnly/>
              </VendorConfig>
            </Port>
            <ReadOnly/>
          </Ports>
          <Position/>
          <ReadOnly/>
          <SerialIdentifier/>
          <VendorConfig>
            <NameValues>
              <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
              <Owner/>
              <ReadOnly/>
            </NameValues>
            <Owner/>
            <ReadOnly/>
          </VendorConfig>
        </SubModule>
        <VendorConfig>
          <NameValues>
            <xsl:copy-of select="/MDLRoot/NetworkDomains/Network/NetworkNode/LogicalLocation/GenericParameter/NameValue"/>
            <Owner/>
            <ReadOnly/>
          </NameValues>
          <Owner/>
          <ReadOnly/>
        </VendorConfig>
      </Module>
      <Name/>
      <Owner/>
      <ReadOnly/>
    </InternalStructure>
  </xsl:template>
  <!-- Add node "/MDLRoot/TestMissions/TestMission/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/TestMissions/TestMission/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/TestMissions/TestMission/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/TestMissions/TestMission/HandoffRules" -->
  <xsl:template match="/MDLRoot/TestMissions/TestMission">
    <HandoffRules>
      <HandoffRule>
        <Description/>
        <Name/>
        <Owner/>
        <ReadOnly/>
        <Rule/>
      </HandoffRule>
      <Owner/>
      <ReadOnly/>
    </HandoffRules>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap">
    <TimeOffset/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataMap/DataWordToFieldMap">
    <TimeOffsetIncrement/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/DataStructure/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/MILSTD1553Messages/MILSTD1553Message/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap">
    <TimeOffset/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataMap/DataWordToFieldMap">
    <TimeOffsetIncrement/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataFieldSet/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/DataStructure/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/ARINC429Messages/ARINC429Message/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/PCMDataLink/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/PCMDataLink/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/PCMDataLink/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap">
    <TimeOffset/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataMap/DataWordToFieldMap">
    <TimeOffsetIncrement/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataFieldSet/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/DataStructure/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataStreams/DataStream/GenericDataStreamMessages/GenericDataStreamMessage/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes/TimeAttributes" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/DataAttributes">
    <TimeAttributes>
      <Owner/>
      <ReadOnly/>
      <TimestampFormat/>
      <TimestampType/>
    </TimeAttributes>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/ProperName" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement">
    <ProperName/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement/MeasurementTimeRef" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Measurements/Measurement">
    <MeasurementTimeRef/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Messages/MessageDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Messages/MessageDefinition/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Messages/MessageDefinition/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/DataOperations/DataOperation/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/DataOperations/DataOperation/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/DataOperations/DataOperation/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap">
    <TimeOffset/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/DataMap/DataWordToFieldMap">
    <TimeOffsetIncrement/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/Syllable/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/DataWord/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/TimeOffset" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap">
    <TimeOffset/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap/TimeOffsetIncrement" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/DataMap/DataWordToFieldMap">
    <TimeOffsetIncrement/>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/NonTmNSPackageHeaderFields/FieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/NonTmNSPackageHeaderFields/FieldDescription/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/NonTmNSPackageHeaderFields/FieldDescription/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageLengthField/PackageLengthFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageLengthField/PackageLengthFieldDescription/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageLengthField/PackageLengthFieldDescription/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/TimestampDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/TimestampDefinition/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/TimestampDefinition/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PackageTimeDeltaFieldDescription/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/StatusFlagFields/FieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/StatusFlagFields/FieldDescription/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/StatusFlagFields/FieldDescription/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PDIDFieldDescription/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PDIDFieldDescription/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/PackageHeaderDefinition/PackageHeaderStructure/PDIDFieldDescription/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageDefinition/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataFieldSet/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataField/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataField/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/PackageDataField/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/MeasurementDomains/MeasurementDomain/Packages/PackageStructure/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Add node "/MDLRoot/Units/DerivedUnit/GenericParameter/NameValues" -->
  <xsl:template match="/MDLRoot/Units/DerivedUnit/GenericParameter">
    <NameValues>
      <xsl:copy-of select="/MDLRoot/Units/DerivedUnit/GenericParameter/NameValue"/>
      <Owner/>
      <ReadOnly/>
    </NameValues>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Device/ModelNumber" to "Model"-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/ModelNumber">
    <xsl:element name="Model">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Device/SerialNumber" to "SerialIdentifier"-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/SerialNumber">
    <xsl:element name="SerialIdentifier">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Antenna/ModelNumber" to "Model"-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Antenna/ModelNumber">
    <xsl:element name="Model">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Renaming element "/MDLRoot/NetworkDomains/Network/Antenna/SerialNumber" to "SerialIdentifier"-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Antenna/SerialNumber">
    <xsl:element name="SerialIdentifier">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>
  <!-- handle removals-->
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/Sensitivity"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/Connector"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/Excitation"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/Device/Ports"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/TmNSManageableApps/TmNSManageableApp/TmNSDAU/Module"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/Connector"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/Ports"/>
  <xsl:template match="/MDLRoot/NetworkDomains/Network/NetworkNode/NetworkInterface"/>
  <xsl:template match="/MDLRoot/TestMissions/TestMission/RadioLinks/RadioLink/LinkControlMode"/>
</xsl:stylesheet>
