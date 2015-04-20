### Overview:
-------------
Simple demonstration of how to build Async mail sender based on Spring Boot, Redis, MySQL which can process more than 400 mails/second. You can use this, customize it the best way it suits you.


### Pre-requisites:
-------------------
    * Redis is installed.
    * MySQL is up and running.
    * SMTP Server for testing - I use (https://nilhcem.github.io/FakeSMTP/)


### Setup
---------
```bash
$ git glone <git-project-url>
```

```bash
$ edit src/main/resources/application.properties, and set the values as per your environment.
```


### Run and Test it!
--------------------
Use Curl (or POSTman) to run REST APIs to test it.

  ## Populate the test data
  ```bash
  $ curl -X POST "http://localhost:8080/email/testdata?rows=500"
  ```

  ## Get and view the test data
  ```bash
  $ curl -X GET "http://localhost:8080/email/testdata"
  ```


  ## Trigger emails that are populated above (make sure smtp server is running)
  ```bash
  $ curl -X PUT "http://localhost:8080/email/trigger"  
```

  ## Delete all records
  ```bash
  $ curl -X DELETE "http://localhost:8080/email"
  ```


### TODO:
---------
    * Add Support for distributed locking of records, so redundant nodes, don't pick up same records.
    * Unit testing.
    * Support for NoSQL (pretty easy)
    * Publish the benchmarking.
    * Pagination/batchSize support.
