# Subscription system challenge
Possible implementation of a subscription system oriented to microservices, where consumers subscribe to 
newsletters via email.

A subscription Rest API is provided, where the frontend can make calls in order to add new subscribers, see the 
status of a subscription, or cancel it.

# Solution overview
A high level architecture diagram is available in [*diagram.png*](diagram.png).
Each component is designed to follow a single responsibility approach.

#### Gateway (public service)
Entry point to the whole system.
It is the only service published to the public network, which handles all incoming requests and forwards them to the 
appropriate microservice behind.

The Gateway is in charge of routing, security, and access control.

#### Subscription backend
This microservice is in charge of the CRUD operations related to the subscriptions, any calculations, 
and managing their persistence.

It is only visible in the private network, the only way to get to it is through the Gateway.
The same would apply to any other microservice added to the system.

Notifications about subscriptions are sent to a Kafka topic.

#### Email sender
This component reads pending notification messages from a Kafka topic and sends email confirmations for subscription 
creation/cancellation.

As it is not guaranteed to give a response in an acceptable time, 
the Kafka topic that serves as notification queue provides fault-tolerancy, 
without blocking the Backend until the whole email sending process is done.

Wrong messages can be sent to a Dead Letter Queue topic for more fault-tolerancy and analysis.

# Implementation details
#### Gateway
Uses Spring Gateway, which makes it easy to get started with some routing features working out of the box. 
It also allows to implement custom filters if the system needed them.
If new microservices that required authentication were included in the system (for example, employees only), 
it would be possible to configure SAML authentication with Spring Security.

The routing configuration required for the challenge is in the 
[application.yml](subscription-gateway/src/main/resources/application.yml) file.

#### Subscription backend
Exposes an API using Spring @RestController and persists the changes in the database abstracted by a JpaRepository and 
Hibernate (Object-Relational Mapper).

The API is documented with Swagger using the library Springfox in order to generate it automatically.

The database used is H2 (in-memory database) for simplicity. An external DB can be used by configuration.

It uses Spring-Kafka to produce notification messages into a Kafka topic defined in the configuration.

#### Email sender
It uses Spring-Kafka @KafkaListener to read the pending notifications from a topic and send them as emails.

Also, it implements a custom error handler (with Spring ErrorHandlingDeserializer) to handle deserialization errors
and send the message to a Dead Letter Queue topic if enabled.

#### Testing
There are Spring Application tests for all meaningful code implemented.

# Alternatives
#### Kong as gateway
A managed Kong service (https://konghq.com/) could replace the Gateway, as it also implements routing, security 
and access control lists.

# Bonuses
#### CI/CD
A [Jenkins CI/CD pipeline stub](subscription-backend/Jenkinsfile) is included for all components. The steps are:

1) Build, test, and analyze the code (i.e. with Sonar scanner)
2) For the main branch (when a PR is merged), build a new Docker image and push it to the registry (Harbor).
3) Deploy the new version on Kubernetes using the component's Helm chart

#### Kubernetes files
Each component has a [./kubernetes](subscription-backend/kubernetes) folder in the repo containing the templates 
for its Deployment, Service, and Ingress (in the case of the Gateway).

The environment variables in the template would be filled when applying the Helm chart (template syntax is simplified
for the challenge), using the values.yml files under the [./config](subscription-backend/config) folder 
for each environment.

The internal microservices that expose endpoints use a Service to expose them internally in the cluster,
making them available only from the Gateway and not for the outside.

The Gateway uses an Ingress that is associated with its Service, exposing it to the public network, 
and securing the data in transit using HTTPS with a TLS certificate stored in a secret.

As the Gateway and Backend are critical, it would be a good practice to deploy multiple replicas or a 
[Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/) for high 
availability. 

# How to build locally
Each component folder has a [*docker_build.sh*](subscription-backend/docker_build.sh) file that can be executed 
to build the Jar with Maven and build a Docker image with it using the Dockerfile in the same folder.

If you run into any issue, the images are available publicly on Docker Hub 
([gonzalotp](https://hub.docker.com/u/gonzalotp))

# How to run locally
#### With Kafka
1) Use [Confluent's cp-all-in-one docker-compose](https://github.com/confluentinc/cp-all-in-one).
This has been tested with version 6.0.0.
Clone the repo and use *sudo docker-compose up -d* in the *cp-all-in-one* folder to run all their containers.
You will know that they are ready visiting *localhost:9021*, where the Control Centre is exposed.

2) Run *sudo docker-compose up -d* in this repository. The containers are configured to only expose the Gateway,
and to connect using the internal Docker network among them and also with Kafka.
By default and for simplicity, the topics are created automatically when using them for the first time.

3) The base URL for the subscription resources is *localhost:9998/subscriptions*

#### Without Kafka
1) Set the KAFKA_ENABLED=true variable to false in *docker-compose.yml* in the *subscription-backend* container.

2) Run *sudo docker-compose up -d* in this repository. The email service will not be able to consume anything, but
it will be possible to create subscriptions.

3) The base URL for the subscription resources is *localhost:9998/subscriptions*

# How to use
To see how to use the API, Swagger UI is exposed at *localhost:9998/subscriptions/documentation/swagger-ui/* 
(notice the / at the end!)

An API testing tool such as Postman can be used to make the calls.