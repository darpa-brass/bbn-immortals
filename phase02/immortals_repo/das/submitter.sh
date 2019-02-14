#!/usr/bin/env bash

START_TIME=`date +%s%N | cut -b1-13`

HELP="
INVALID INPUT!

Valid argument values:
    cp1baselinea
    cp1baselineb
    cp1
    cp2baselinea
    cp2baselineb
    cp2
    cp3baselinea
    cp3hddrassbaselineb
    cp3hddrass
    cp3plugbaselineb
    cp3plug
    cp3litlbaselineb
    cp3litl
"

DISABLE_DAS_INPUT="
{
  \"dasEnabled\": \"false\"
}
"

CP1_INPUT="
{
  \"sessionIdentifier\" : \"CP1${START_TIME}\",
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
  \"sessionIdentifier\" : \"CP2${START_TIME}\",
  \"globalModel\": {
    \"requirements\": {
      \"dataInTransit\": {
        \"securityStandard\": \"AES_128\"
      }
    }
  },
  \"atakLiteClientModel\": {
    \"resources\": [
      \"STRONG_CRYPTO\"
    ]
  },
  \"martiServerModel\": {
    \"resources\": [
      \"HARWARE_AES\",
      \"STRONG_CRYPTO\"
    ]
  }
}
"

CP3_HDDRASS_INPUT="
{
    \"sessionIdentifier\" : \"CP3HDDRASS${START_TIME}\",
    \"martiServerModel\": {
        \"requirements\": {
            \"libraryUpgrade\": \"ElevationApi_2\"
        }
    }
}
"

CP3_PLUG_INPUT="
{

  \"sessionIdentifier\" : \"CP3PLUG${START_TIME}\",
  \"atakLiteClientModel\": {
    \"requirements\": {
      \"partialLibraryUpgrade\": \"Dropbox_3_0_6\"
    }
  }
}
"

CP3_LITL_INPUT="
{
  \"sessionIdentifier\" : \"CP3LITL${START_TIME}\",
  \"atakLiteClientModel\": {
    \"requirements\": {
      \"deploymentPlatformVersion\": \"Android23\"
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

    elif [ "$1" = "cp2baselineb" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" -d "$CP2_INPUT" http://127.0.0.1:55555/action/crossApplicationDependencies

    elif [ "$1" = "cp2" ];then
        curl -X POST -H "Content-Type: application/json" -d "$CP2_INPUT" http://127.0.0.1:55555/action/crossApplicationDependencies

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

    elif [ "$1" = "cp3litlbaselineb" ];then
        curl -X POST -H "Content-Type: application/json" -d "$DISABLE_DAS_INPUT"  http://127.0.0.1:55555/enabled
        sleep 2
        curl -X POST -H "Content-Type: application/json" -d "$CP3_LITL_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    elif [ "$1" = "cp3litl" ];then
        curl -X POST -H "Content-Type: application/json" -d "$CP3_LITL_INPUT" http://127.0.0.1:55555/action/libraryEvolution

    else
        echo ${HELP}
    fi

fi
