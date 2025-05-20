# Kafka Avro Configuration Properties

This document describes the expected properties for configuring Kafka Avro Producer and Consumer in your Spring Boot application.

## Common Properties

| Property Name                        | Description                                                                 | Example Value(s)                | Required |
|--------------------------------------|-----------------------------------------------------------------------------|----------------------------------|----------|
| `spring.kafka.bootstrap-servers`     | Comma-separated list of Kafka broker addresses.                             | `localhost:9092`                 | Yes      |
| `spring.kafka.schema.registry.url`   | URL of the Confluent Schema Registry.                                       | `http://localhost:8081`          | Yes      |
| `spring.kafka.security-protocol`     | Security protocol to use for Kafka connection (e.g., PLAINTEXT, SASL_SSL).  | `PLAINTEXT`, `SASL_SSL`          | Yes      |
| `spring.kafka.sasl.mechanism`        | SASL mechanism for authentication (if applicable).                          | `PLAIN`, `SCRAM-SHA-512`         | Yes      |
| `spring.kafka.sasl.username`         | Username for SASL authentication (if applicable).                           | `myuser`                         | No*      |
| `spring.kafka.sasl.password`         | Password for SASL authentication (if applicable).                           | `mypassword`                     | No*      |

\* Required if SASL authentication is enabled.

## Consumer-Specific Properties

| Property Name                        | Description                                                                 | Example Value(s)                | Required |
|--------------------------------------|-----------------------------------------------------------------------------|----------------------------------|----------|
| `spring.kafka.consumer.group-id`     | Consumer group ID for Kafka consumers.                                      | `my-consumer-group`              | Yes      |

## Notes

- All properties should be defined in your `application.properties` or `application.yaml` file.
- The producer and consumer configurations use Avro serialization/deserialization with Confluent Schema Registry.
- Security-related properties are only required if your Kafka cluster is secured.