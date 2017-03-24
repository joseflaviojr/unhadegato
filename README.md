# Unha-de-gato

Concentrador de [Copaíbas](http://joseflavio.com/copaiba).

Concentrator of [Copaíbas](http://joseflavio.com/copaiba).

## Versão Atual / Current Version

1.0-A11

Padrão de versionamento: [JFV](http://joseflavio.com/jfv)

## Requisitos / Requirements

* Git >= 1.8
* Java >= 1.8
* Gradle >= 2.0

## Servidor / Server (Docker Container)

Download and compilation:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A11.zip
    unzip 1.0-A11.zip
    cd unhadegato-1.0-A11
    gradle dist

Image:

    docker build --force-rm -t joseflavio/unhadegato:1.0-A11 .

Volume:

    docker volume create --name unhadegato

Running:

    docker run --name="unhadegato" -d -p 8885:8885 -p 8886:8886 -v unhadegato:/volume --ip=x.x.x.x --net xxxxxx --restart=unless-stopped joseflavio/unhadegato:1.0-A11

Configuration:

    cd /var/lib/docker/volumes/unhadegato/_data/conf

Log:

    tail /var/lib/docker/volumes/unhadegato/_data/logs/unhadegato.log -n 100

Removal:

    docker rm -f unhadegato
    docker rmi joseflavio/unhadegato:1.0-A11

## Servidor tradicional / Traditional server

Download, compilation and running:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A11.zip
    unzip 1.0-A11.zip
    cd unhadegato-1.0-A11
    gradle dist
    cd build/dist
    ./unhadegato.sh

## Uso como cliente / Use as client

Gradle:

    compile 'com.joseflavio:unhadegato:1.0-A11'

Maven:

    <dependency>
        <groupId>com.joseflavio</groupId>
        <artifactId>unhadegato</artifactId>
        <version>1.0-A11</version>
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
