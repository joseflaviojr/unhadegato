#------------------------------

# Unha-de-gato

#------------------------------

FROM java:8-jre-alpine

MAINTAINER Jose Flavio de Souza Dias Junior <contato@joseflavio.com>

#------------------------------

ENV UNHADEGATO /usr/local/unhadegato
ENV PATH ${UNHADEGATO}/bin:$PATH

#------------------------------

RUN mkdir -p ${UNHADEGATO}/bin && \
    mkdir -p /volume/conf && \
    mkdir -p /volume/logs

ADD build/dist/*.jar ${UNHADEGATO}/bin/
ADD build/dist/*.sh ${UNHADEGATO}/bin/
# ADD build/dist/*.conf /volume/conf/

RUN chmod -R 755 ${UNHADEGATO}/bin && \
    ln -s /volume/logs ${UNHADEGATO}/bin/log

#------------------------------

WORKDIR ${UNHADEGATO}/bin

EXPOSE 8885

CMD ["unhadegato.sh", "/volume/conf"]

#------------------------------