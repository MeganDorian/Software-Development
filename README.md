# CLI


## Требования для установки:

- JRE java 9 и выше
- [Gradle 7.5.1](https://docs.gradle.org/current/userguide/installation.html#installation) или выше

## Сборка проекта

Чтобы собрать проект, необходимо выполнить следующие команды:

Windows:

```shell
./gradlew clean build
```

Linux:
```shell
gradle clean build
```

Для запуска только тестов необходимо выполнить команду:

```shell
./gradlew test
```

## Запуск
Для запуска `.jar` файла, в консоли необходимо перейти в папку, где лежит файл и ввести команду командой
```shell
java -jar {fileName}.jar
```
