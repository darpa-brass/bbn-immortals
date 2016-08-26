sudo docker build --tag=java_docker:latest docker/java_docker
sudo docker build --tag=java_docker:0.1 docker/java_docker
sudo docker build --tag=android_docker:latest docker/android_docker
sudo docker build --tag=android_docker:0.1 docker/android_docker

if [ -e "android_staticanalysis/bbnAnalysis.tar.gz" ];then
  sudo docker build --tag=android_staticanalysis:latest docker/android_staticanalysis
fi
