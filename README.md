# Unha-de-gato

Concentrador, compartilhador e distribuidor de [Copaíbas](http://joseflavio.com/copaiba).

Concentrator, sharer and distributor of [Copaíbas](http://joseflavio.com/copaiba).

## Versão Atual / Current Version

1.0-A8

Padrão de versionamento: [JFV](http://joseflavio.com/jfv)

## Requisitos / Requirements

* Git >= 1.8
* Java >= 1.7
* Gradle >= 2.0

## Servidor / Server (Docker Container)

Download and compilation:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A8.zip
    unzip 1.0-A8.zip
    cd unhadegato-1.0-A8
    gradle dist

Image:

    docker build --force-rm -t joseflavio/unhadegato:1.0-A8 .

Volume:

    docker volume create --name unhadegato

Running:

    docker run --name="unhadegato" -d -p 8885:8885 -v unhadegato:/volume --ip=x.x.x.x --net xxxxxx --restart=unless-stopped joseflavio/unhadegato:1.0-A8

Configuration:

    cd /var/lib/docker/volumes/unhadegato/_data/conf

Log:

    tail /var/lib/docker/volumes/unhadegato/_data/logs/unhadegato.log -n 100

Removal:

    docker rm -f unhadegato
    docker rmi joseflavio/unhadegato:1.0-A8

## Servidor tradicional / Traditional server

Download, compilation and running:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A8.zip
    unzip 1.0-A8.zip
    cd unhadegato-1.0-A8
    gradle dist
    cd build/dist
    ./unhadegato.sh

## Uso como cliente / Use as client

Gradle:

    compile 'com.joseflavio:unhadegato:1.0-A8'

Maven:

    <dependency>
        <groupId>com.joseflavio</groupId>
        <artifactId>unhadegato</artifactId>
        <version>1.0-A8</version>
    </dependency>

## Desenvolvimento / Development

Configuração do projeto para Eclipse ou IntelliJ IDEA.

Project configuration for Eclipse or IntelliJ IDEA.

    gradle eclipse
    gradle cleanIdea idea

## Compilação / Compilation

Parcial/Partial:

    gradle clean dist

Completa/Complete:

    gradle clean build
