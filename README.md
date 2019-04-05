# CSYE 6225 - Spring 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
|Jin Zhang|001899149|zhang.jin2@husky.neu.edu |
|Yanjuan Li|001497203|li.yanj@husky.neu.edu |
|Jingyi Cui|001493484|cui.jingy@husky.neu.edu |
|Xinxin Huang|001898856|huang.xinx@husky.neu.edu|

## Technology Stack
Linux environment
Spring Framework
Tomcat as server
REST Repositories
Java 
MySQL
JDBC
JSON
IntelliJ
Postman
Git
Amazon(EC2 S3 Lambda)
CircleCI

## Build Instructions
User class: user attributes
UserRepository class: get, select, update, delete users
UserService class: the main controller
BCrypt class: downloaded from "http://www.mindrot.org/projects/jBCrypt/", store password securely
Token creation: encoded from user's email and password

## Deploy Instructions
Deploy the application locally on Tomcat

Deploy on AWS 
"$bash csye6225-aws-cf-create-policy.sh policy CirclrCI domain"
"$bash csye6225-aws-cf-create-stack.sh networkname network"
"$bash csye6225-aws-cf-create-auto-scaling-application-stack.sh applicationstack network ami-xxxxxxxxxxx bucketname"

## Running Tests
Since we only developed the backend, we used Postman to test the functions.

## CI/CD



