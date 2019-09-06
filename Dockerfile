FROM java:8-alpine

WORKDIR /
RUN apk update \
    && apk add curl \
    && apk add tzdata
ADD target/JavaBaas-boot.jar target/JavaBaas-boot.jar
ENV TZ Asia/Shanghai

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

EXPOSE 8080
CMD java -jar target/JavaBaas-boot.jar