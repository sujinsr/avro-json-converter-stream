FROM maven:3.6.2-jdk-8

ARG kafka_url
ARG schema_registry_url
ARG topic_list

ENV kafka_url ${kafka_url}
ENV schema_registry_url ${schema_registry_url}
ENV topic_list ${topic_list}

RUN mkdir /app

RUN cd /app && git clone https://github.com/sujinsr/avro-json-converter-stream.git && \
    cd avro-json-converter-stream && mvn clean install && cp target/avro-json-converter-stream.jar /app

CMD java -jar /app/avro-json-converter-stream.jar ${kafka_url} ${schema_registry_url} ${topic_list}


