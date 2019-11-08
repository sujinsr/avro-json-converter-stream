FROM maven:3.6.2-jdk-8

RUN mkdir /app

RUN cd /app && git clone https://github.com/sujinsr/avro-json-converter-stream.git && \
    cd avro-json-converter-stream && mvn clean install && cp target/avro-json-converter-stream.jar /app

CMD java -jar /app/avro-json-converter-stream.jar kafka:29092 schema-registry:8081 eot


