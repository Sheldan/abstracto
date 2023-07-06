# Abstracto

![Build](https://github.com/Sheldan/abstracto/workflows/Execute%20Build/badge.svg)
[![GitHub license](https://img.shields.io/github/license/Sheldan/abstracto)](https://github.com/Sheldan/abstracto/blob/master/LICENSE)


Abstracto represents a framework to be used as a basis for a Discord bot. It uses [JDA](https://github.com/DV8FromTheWorld/JDA/) as an API wrapper underneath
and provides an extensive tool set to create new commands and a wide range of commands out of the box.

This repository does not provide the full functionality in order to start a discord bot, because it requires a Main class. 
An example implementation of this bot can be seen [here](https://github.com/Sheldan/Crimson). This repository contains the required configuration in order to run a bot and example customizations.


## Technologies
* [JDA](https://github.com/DV8FromTheWorld/JDA/) The Discord API Wrapper in the version 5.0.0-beta.5
* [Spring boot](https://github.com/spring-projects/spring-boot) is used as a framework to create standalone application in Java with Java EE methods. (including dependency injection and more)
* [Hibernate](https://github.com/hibernate/hibernate-orm) is used as a reference implementation of JPA.
* [Freemarker](https://github.com/apache/freemarker) is used as a templating engine. This is used to provide internationalization for user facing text and enable dynamic embed configuration.
* [Ehcache](https://github.com/ehcache/ehcache3) is used as a caching implementation.
* [Lombok](https://github.com/rzwitserloot/lombok) is used as a framework in order to speed up creation of container classes and builders.
* [Quartz](https://github.com/quartz-scheduler/quartz) is used as a scheduling framework in order to provide functionalities which either require a scheduled or cronjob behaviour.
* [Docker](https://github.com/docker) is used to package the application into an image and [Docker Compose](https://github.com/docker/compose) is used to build the images
* [Liquibase](https://github.com/liquibase/liquibase) is used to manage changes to the database

## Documentation
A detailed documentation of the pure form of Abstracto including the terminology and commands in HTML form is available [here](https://sheldan.github.io/abstracto-docs/current). The PDF is available [here](https://sheldan.github.io/abstracto-docs/current/documentation.pdf)
If you want to view the documentation to an earlier released version you need to append the desired version to the URL. The current version will be available aforementioned URL, but it is not right now, because Abstracto has not been released yet.


## Issues
If you find any issue, feel free to create a GitHub issue.

## License
This project is licensed under the MIT license.