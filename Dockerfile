FROM java:8-alpine

WORKDIR /
ADD target /target

HEALTHCHECK --interval=5s --timeout=3s CMD curl --fail http://localhost:8080/ || exit 1
EXPOSE 8080
CMD java -jar target/JavaBaas-boot.jar