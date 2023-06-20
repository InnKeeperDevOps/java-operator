FROM docker.io/openjdk:19-oraclelinux8

RUN microdnf install findutils

COPY . /

RUN ls /gradle

CMD ./gradlew run