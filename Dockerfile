FROM docker.io/openjdk:19-oraclelinux8 as builder

RUN microdnf install findutils

COPY . /

RUN ls /gradle

RUN ./gradlew build

FROM docker.io/openjdk:19-oraclelinux8 as runner

COPY --from=builder /build/distributions/innkeeper-1.0.zip /

RUN microdnf install unzip

RUN unzip /innkeeper-1.0.zip

WORKDIR /innkeeper-1.0/

CMD bin/innkeeper