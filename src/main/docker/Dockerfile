FROM openjdk:alpine
VOLUME /tmp
ADD systemhub-test-app-*.jar systemhub-test-app.jar
RUN sh -c 'touch /systemhub-test-app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/systemhub-test-app.jar"]
EXPOSE 8888