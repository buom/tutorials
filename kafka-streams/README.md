# Kafka Streams Example

## 🌊 Getting Started

Open the terminal and change the current directory to:

```
cd kafka-streams
```

Start the Docker containers for Zookeeper, Kafka and Schema Registry:

```
docker-compose up
```

On a different terminal, build the project:

```
../gradlew build
```

Start an instance of the application:

```
../gradlew run
```

On a different terminal, try creating preferences with currency USD:

```
curl --header "Content-Type: application/json" --request POST --data '{"customerId":"ebbcf888-f83e-4055-9266-61b51dbf765c","currency":"USD","country":"US"}' http://localhost:5050/api/preferences.create
```

You should see something like this in the logs:

```
Sent PreferencesCreated to topic: preferences
```

Try creating preferences with currency EUR:

```
curl --header "Content-Type: application/json" --request POST --data '{"customerId":"ebbcf888-f83e-4055-9266-61b51dbf765c","currency":"EUR","country":"US"}' http://localhost:5050/api/preferences.create
```

You should see something like this in the logs:

```
Sent CreatePreferencesDenied to topic: preferences-authorization
```

## 🏄🏻‍♀️ Tutorial

For a more detailed explanation of this example of Kafka Streams, please check the following tutorial: [Introduction to Kafka Streams in Kotlin](https://code.parts/2020/06/13/kafka-streams-kotlin/)
