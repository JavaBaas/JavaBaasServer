FROM maven:alpine

WORKDIR /code

ADD pom.xml /code/pom.xml
ADD src /code/src
RUN mvn package -DskipTests

CMD java -jar target/JavaBaas-2.0.0.jar