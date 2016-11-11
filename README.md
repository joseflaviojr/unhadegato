# Unha-de-gato

Concentrador, compartilhador e distribuidor de [Copaíbas](http://joseflavio.com/copaiba).

Concentrator, sharer and distributor of [Copaíbas](http://joseflavio.com/copaiba).

## Versão Atual / Current Version

1.0-A6

Padrão de versionamento: [JFV](http://joseflavio.com/jfv)

## Requisitos / Requirements

* Java >= 1.7
* Gradle >= 2.0

## Servidor / Server (Docker Container)

Download and compilation:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A6.zip
    unzip 1.0-A6.zip
    cd unhadegato-1.0-A6
    gradle dist

Dockerizing:

    docker build --force-rm -t joseflavio/unhadegato:1.0-A6 .

Creating the volume:

    docker volume create --name unhadegato

Running:

    docker run --name="unhadegato" -d -p 8885:8885 -v unhadegato:/volume --restart=unless-stopped joseflavio/unhadegato:1.0-A6

Connecting to the network:

    docker network connect --ip=x.x.x.x NETWORK_NAME unhadegato

## Servidor tradicional / Traditional server

Download, compilation and running:

    wget https://github.com/joseflaviojr/unhadegato/archive/1.0-A6.zip
    unzip 1.0-A6.zip
    cd unhadegato-1.0-A6
    gradle dist
    cd build/dist
    ./unhadegato.sh

## Uso como cliente / Use as client

Gradle:

    compile 'com.joseflavio:unhadegato:1.0-A6'

Maven:

    <dependency>
        <groupId>com.joseflavio</groupId>
        <artifactId>unhadegato</artifactId>
        <version>1.0-A6</version>
    </dependency>

## Desenvolvimento / Development

Execute o comando a seguir e importe o projeto no Eclipse IDE.

Run the following command and import the project in Eclipse IDE.

    gradle eclipse

## Compilação / Compilation

    gradle clean build
