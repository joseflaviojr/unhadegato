#------------------------------

FROM openjdk:slim

LABEL description="Unha-de-gato 1.0-A16"
LABEL maintainer="Jose Flavio de Souza Dias Junior <email@joseflavio.com>"

#------------------------------

ENV UNHADEGATO /usr/local/unhadegato
ENV PATH ${UNHADEGATO}/bin:$PATH

#------------------------------

RUN mkdir -p ${UNHADEGATO}/bin && \
    mkdir -p /volume/conf && \
    mkdir -p /volume/dados && \
    mkdir -p /volume/logs

ADD build/dist/*.jar ${UNHADEGATO}/bin/
ADD build/dist/*.sh ${UNHADEGATO}/bin/

RUN chmod -R 755 ${UNHADEGATO}/bin && \
    ln -s /volume/logs ${UNHADEGATO}/bin/log

#------------------------------

WORKDIR ${UNHADEGATO}/bin

EXPOSE 8885
EXPOSE 8886

CMD ["unhadegato.sh", "/volume/conf"]

#------------------------------