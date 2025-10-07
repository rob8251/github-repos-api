# Application
A `Spring Boot` web application that exposes a REST API for retrieving information about a `GitHub` user’s non-fork repositories.  
The application communicates with the GitHub API using a reactive `WebClient`.

# Information

- Java 21
- Spring Boot 3
- WebClient for making HTTP requests to the Github API
- WireMock and WebTestClient for testing the application

# Endpoints
`GET /api/repos/{username}`

# Response
The application returns a JSON array of non-fork repositories.  
For each repository, the following information is provided:

- **Repository name**
- **Owner's username**
- **Branches** (each branch’s name and last commit SHA)

# Error handling

In case of errors (user not found), the API returns a JSON response with the following structure:

```json
{
    "status": 404,
    "message": "User not found"
}
```
# Testing
The project includes integration tests (`RepoControllerIT`) written with **Spring Boot Test**, **WireMock**, and **WebTestClient**.  
They verify that the API correctly handles different scenarios, such as returning non-fork repositories, handling empty results, and responding properly to missing users.

To run the tests, use the command: `./mvnw test`

# Running the Application

1. Ensure you have Java 21 installed on your machine.

2. Clone the repository.

3. Navigate to the project directory.

4. Run the application using the command: `./mvnw spring-boot:run`


