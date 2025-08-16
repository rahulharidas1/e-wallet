# E-Wallet Microservices Application
This is a distributed E-Wallet Application built using Spring Boot and following the microservices architecture. The system handles user registration, wallet operations, transaction processing, and notifications. Communication between services is handled asynchronously using Apache Kafka.

## Architecture Overview
The application is composed of the following services:
| Service                | Description                                                          |
| ---------------------- | -------------------------------------------------------------------- |
| `user-service`         | Manages user account creation and details. Uses Redis for caching user data. |
| `wallet-service`       | Handles wallet operations like wallet creation and balance updates.  |
| `transaction-service`  | Manages the sending and receiving of funds between users.            |
| `notification-service` | Sends email notifications related to transaction activities.    |

## Tech Stack

* Spring Boot
* Apache Kafka (asynchronous inter-service communication)
* Redis (caching user data)
* Spring Data JPA
* MySQL
