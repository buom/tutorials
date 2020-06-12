# Kafka Interactive Queries Example

## 🌊 Getting Started

Open the terminal and change the current directory to:

```
cd kafka-interactive-queries
```

Start the Docker containers for Zookeeper, Kafka and Schema Registry:

```
docker-compose up
```

On a different terminal, build the project:

```
../gradlew build
```

Start an instance of the application on port 5051:

```
RATPACK_PORT=5051 ../gradlew run
```

On a different terminal, start an instance of the application on port 5052:

```
RATPACK_PORT=5052 ../gradlew run
```

On a different terminal, try adding funds multiple times:

```
curl --header "Content-Type: application/json" --request POST --data '{"customerId":"ebbcf888-f83e-4055-9266-61b51dbf765c","amount":"5.00"}' http://localhost:5051/api/balance.addFunds
```

Try getting the balance on both port 5051 and port 5052:

```
curl --request GET http://localhost:5051/api/customers.getBalance?customerId=ebbcf888-f83e-4055-9266-61b51dbf765c
curl --request GET http://localhost:5052/api/customers.getBalance?customerId=ebbcf888-f83e-4055-9266-61b51dbf765c
```

You should be able to get the balance from both instances and if you check the logs of the two running instances you should be able to see something like this:

```
Proxy to remote state store on localhost:5052
Reading local state store on localhost:5052
```

## 🏄🏻‍♀️ Tutorial

For a more detailed explanation of this example of Kafka Interactive Queries, please check the following tutorial: [Create a Read Model using Kafka Streams State Stores](https://code.parts/2020/03/08/create-a-read-model-using-kafka-streams-state-stores/)
