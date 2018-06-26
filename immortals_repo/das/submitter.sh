#!/usr/bin/env bash


HELP="
INVALID INPUT!

Valid argument values: cp1baselinea, cp1baselineb, cp1, cp2baselinea, cp3baselinea, cp3hddrassbaselineb, cp3plugbaselineb, cp3hddrass, cp3plug
"

DISABLE_DAS_INPUT="
{
  \"dasEnabled\": \"false\"
}
"

CP1_INPUT="
{
  \"martiServerModel\": {
    \"requirements\": {
      \"postgresqlPerturbation\": {
        \"tables\": [
        {
          \"columns\": [
            \"CotEvent_SourceId\",
            \"CotEvent_CotType\",
            \"CotEvent_How\",
            \"CotEvent_Detail\",
            \"CotEvent_ServerTime\"
          ]
        },
        {
          \"columns\": [
            \"Position_PointHae\",
          \"Position_PointCE\",
          \"Position_PointLE\",
          \"Position_TileX\",
          \"Position_TileY\",
          \"Position_Longitude\",
          \"Position_Latitude\"
          ]
        }
        ]
      }
    }
  }
}
"

CP2_INPUT="
{
  \"sessionIdentifier\" : \"Cp2Challenge\",
  \"globalModel\": {
    \"requirements\": {
      \"dataInTransit\": {
        \"securityStandard\": \"NIST800Dash171\"
      }
    }
  }
}
"

CP3_HDDRASS_INPUT="
{
    \"sessionIdentifier\": \"1337Model\",
    \"martiServerModel\": {
        \"requirements\": {
            \"libraryUpgrade\": \"ElevationApi_2\"
        }
    }
}
"

CP3_PLUG_INPUT="
{
  \"sessionIdentifier\": \"1337Model\",
  \"atakLiteClientModel\": {
    \"requirements\": {
      \"deploymentPlatformVersion\": \"Android21\",
      \"partialLibraryUpgrade\": \"Dropbox_3_0_6\"
    }
  }
}
"

if [ "$#" -ne 1 ];then
    echo ${HELP}

else
    if [ "$1" = "cp1baselinea" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" http://127.0.0.1:55555/action/databaseSchemaPerturbation

    elif [ "$1" = "cp1baselineb" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" -d "$CP1_INPUT" http://127.0.0.1:55555/action/databaseSchemaPerturbation

    elif [ "$1" = "cp1" ];then
        curl -X POST -H "Content-Type: application/json" -d "$CP1_INPUT" http://127.0.0.1:55555/action/databaseSchemaPerturbation

    elif [ "$1" = "cp2baselinea" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" http://127.0.0.1:55555/action/crossApplicationDependencies

    elif [ "$1" = "cp3baselinea" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" http://127.0.0.1:55555/action/libraryEvolution

    elif [ "$1" = "cp3hddrassbaselineb" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" -d "$CP3_HDDRASS_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    elif [ "$1" = "cp3hddrass" ];then
        curl -X POST -H "Content-Type: application/json" -d "$CP3_HDDRASS_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    elif [ "$1" = "cp3plugbaselineb" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" -d "$CP3_PLUG_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    elif [ "$1" = "cp3plug" ];then
        curl -X POST -H "Content-Type: application/json" -d "$CP3_PLUG_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    else
        echo ${HELP}
    fi

fi
