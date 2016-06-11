# Unha-de-gato

Concentrador, compartilhador e distribuidor de [Copaíbas](http://joseflavio.com/copaiba).

Concentrator, sharer and distributor of [Copaíbas](http://joseflavio.com/copaiba).

## Versão Atual / Current Version

1.0-A1

Padrão de versionamento: [JFV](http://joseflavio.com/jfv)

## Requisitos / Requirements

* Java >= 1.7

## Uso como cliente / Use as client

Gradle:

    compile 'com.joseflavio:unhadegato:1.0-A1'

Maven:

    <dependency>
        <groupId>com.joseflavio</groupId>
        <artifactId>unhadegato</artifactId>
        <version>1.0-A1</version>
    </dependency>

## Uso como servidor / Use as server

Download, compilation and running:

    git clone https://github.com/joseflaviojr/unhadegato.git
    cd unhadegato
    gradle build
    cd build/dist
    ./unhadegato.sh

## Desenvolvimento / Development

Execute o comando a seguir e importe o projeto no Eclipse IDE.

Run the following command and import the project in Eclipse IDE.

    gradle eclipse

## Compilação / Compilation

    gradle clean build
