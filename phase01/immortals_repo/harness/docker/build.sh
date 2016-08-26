sudo docker build --tag=java_docker:latest java_docker
sudo docker build --tag=android_docker:latest android_docker

if [ -e "android_staticanalysis/bbnAnalysis.tar.gz" ];then
  sudo docker build --tag=android_staticanalysis:latest android_staticanalysis
else
  echo "android_staticanalysis/bbnAnalysis.tar.gz does not exist!"
fi
