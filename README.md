# Library API
#### Useful badges
[![Build Status](https://www.travis-ci.com/Andrew-2609/library-api.svg?branch=main)](https://www.travis-ci.com/Andrew-2609/library-api)
[![codecov](https://codecov.io/gh/Andrew-2609/library-api/branch/main/graph/badge.svg?token=7QLVDP1ZNR)](https://codecov.io/gh/Andrew-2609/library-api)
## About the project
Library API is a simple (but well tied) RESTFul Spring Boot application, that I made based on an Udemy Course. It is based on loaning and returning books from an online library, with scheduling and e-mail warnings for overdue loans.

It is a small web application based pretty much only in Books and Loans, disregarding entities for Customers, Employees, or such.
### Built with:
* Test Driven Development (with JUnit 5 and AssertJ)
* [Insomnia](https://insomnia.rest) for testing the requests of the API
* [Swagger UI](https://swagger.io) for the documentation of the API (see the project [ documentation](https://ndrewcoding-library-api.herokuapp.com/swagger-ui.html))
* H2 Database, for the application works with inmemory database.
* [Travis CI](https://www.travis-ci.com) for continuous integration of the API
* [Jacoco](https://www.eclemma.org/jacoco) with [Codecov](https://about.codecov.io) for the code coverage of the API (see it on the badges)
* [Heroku](https://ndrewcoding-library-api.herokuapp.com) for the deployment of the API

## Notes
I am very proud of completing this project, even though it is quite simple. At the end of the course, about 81% of the project was covered with tests, and I did my best to use what I learned to make it 100% (in theory).

I also used [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) and Actuator to monitor the application, although its maven project is not here in this repository.

My special thanks to my mate [Leandro](https://github.com/Leandro0101), who has helped me to refactor some code and give me some ideas of how to keep this API as clean and functional as I can.

[Course link (in portuguese)](https://www.udemy.com/course/design-de-apis-restful-com-tdd-spring-boot-e-junit-5)