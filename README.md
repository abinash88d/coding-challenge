# Abinash's Solution

## Enhancement

As Rate files are getting downloaded asynchronously, other services will be up and serves for partial currencies.
A new status service has been added to check the status for all the available currencies.

Status service URL
http://localhost:8080/api/status

Until all the Currencies are ready, api/currencies service will server partial response (with status code 206 and message)

JOB has been sheduled to run automatically everyday 11 AM.
On sheduler services only process partial missing records, not refreshes entire DB.

Properties can be externalize to config server and with extend it can auto refresh without bringing down services using Spring actuator.



## Design
The challenge has been solved in to parts.
  1. First Service downloads rate files asynchronously from Bank's site on boot and loads into H2 database. In real scenario it can be configured with scheduled.
  2. Foreign exchange rate services are exposed without waiting for rates to load.

Technical enhancements
  1. WebClient implementation for async rate file download
  2. Caching implementation
  3. H2 in memory DB with Spring Data JPA for persistence
  4. Swagger for API documentation
  
## Run the App
 
Application can be run as a boot app using below command.

````shell script
$ mvn spring-boot:run
````
Set of test cases is added for unit testing of services.
You can run the test cases with below command.

````shell script
$ mvn test
````

## Service endpoints to use
 		

1)	To get a list of all available currencies
		
		http://localhost:8080/api/currencies  
		
		Sample Request : curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/currencies'	  
		 
	
2)   To get all EUR-FX exchange rates at all available dates
		
		
		
		  http://localhost:8080/api/rates
		  http://localhost:8080/api/rates?page={page}&perPage={size}&sortBy={sortfield}  [with pagination and sorting. Current available sort {rateDate}]
		
		  Sample get all Rates Request : curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/rates'	
		  Sample get Rates with Page and Sort Request : curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/rates?page=1&perPage=2&sortBy=rateDate'
		 
		 
		 
		
3)	To get the EUR-FX exchange rate at particular day. 
	Date Format yyyy-MM-dd (2021-11-12)
		
		  http://localhost:8080/api/rates/{date}  
		
		  Sample get Rates by date Request :curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/rates/2021-11-12'
		
		
	
4)	To get a foreign exchange amount for a given currency converted to EUR on a particular day
	Date Format yyyy-MM-dd (2021-11-12)
		
		  http://localhost:8080/api/convertcurrency/date/{date}/currency/{currencycode}/amount/{amount}
		
		  Sample currency conversion Request :curl -X GET --header 'Accept: application/json' 'http://localhost:8080/api/convertcurrency/date/2021-11-12/currency/AUD/amount/100'
		
	
5)	Swagger Endpoint :
		 
	 http://localhost:8080/swagger-ui.html
 

 
 

# Debugging

 
 Log file path = /logs/currency_convertion_app.log
 
 Rate files path = /dailyrate/files
 
 
 
 
 
# Crewmeister Test Assignment - Java Backend Developer

## Intro
Thank you for taking the time to complete this challenge as part of your application at Crewmeister!
We are taking development skills very serious and invest a lot of time to find the right candidate. 

At Crewmeister we aim to write excellent software and are convinced that this requires a high level of passion for and 
attention to topics such as software design and principles, best practices and clean code. We take pride in the fact
that the code we produce is extensible, testable, maintainable and runs fast.  

At the same time, we always try to improve the effectiveness of our evaluation and improve the candidate journey
throughout the process. Our aim is that our hiring process is mutually inspiring and feels like a gain for
both parties regardless of the outcome. If you feel to give us feedback on that, please don't hesitate to do so. 

## The Challenge

Your task is to create a foreign exchange rate service as SpringBoot-based microservice. 

The exchange rates can be received from [2]. This is a public service provided by the German central bank.

As we are using user story format to specify our requirements, here are the user stories to implement:

- As a client, I want to get a list of all available currencies
- As a client, I want to get all EUR-FX exchange rates at all available dates as a collection
- As a client, I want to get the EUR-FX exchange rate at particular day
- As a client, I want to get a foreign exchange amount for a given currency converted to EUR on a particular day

If you think that your service would require storage, please use H2 for simplicity, even if this would not be your choice if 
you would implement an endpoint for real clients. 

We are looking out for the following aspects in your submission:
- Well structured and thought-through api and endpoint design 
- Clean code
- Application of best practices & design patterns


That being said it is not enough to "just make it work", show your full potential to write excellent software
 for Crewmeister ! 
 
## Setup
#### Requirements
- Java 11 (will run with OpenSDK 15 as well)
- Maven 3.x

#### Project
The project was generated through the Spring initializer [1] for Java
 11 with dev tools and Spring Web as dependencies. In order to build and 
 run it, you just need to click the green arrow in the Application class in your Intellij 
 CE IDE or run the following command from your project root und Linux or ios. 

````shell script
$ mvn spring-boot:run
````

After running, the project, switch to your browser and hit http://localhost:8080/api/currencies. You should see some 
demo output. 


[1] https://start.spring.io/

[2] https://www.bundesbank.de/dynamic/action/en/statistics/time-series-databases/time-series-databases/759784/759784?listId=www_s331_b01012_3
