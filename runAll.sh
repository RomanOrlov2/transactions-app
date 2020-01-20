./gradlew application:clean
./gradlew application:bootJar
./gradlew application-load:clean
./gradlew application-load:bootJar
docker build -t application:latest ./application
docker build -t application-load:latest ./application-load
#docker-compose -f ./.docker/docker-compose.yml up -d --remove-orphans