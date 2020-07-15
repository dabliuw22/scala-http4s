# Http4s API

In this project an API rest with `http4s`, `cats`, `doobie`, `skunk` was created. 
Using **Type-Classes** and **Tagless Final**.

Requirements:
   * JDK >= 1.8
   * Scala 2.13.x
   * Docker
   * Docker Compose
   
1. Run Containers:
    `docker-compose up -d`

2. Run App.
    * Run Migration: `sbt flywayMigrate`
    * Compile: `sbt compile run`
    * Assembly: `sbt assembly`
    * Test: `sbt test`
    * Integration Test: `sbt it:test`

3. Test with cURL:
    * Login:
        ```
        curl --location --request POST 'http://localhost:8080/login' \
        --header 'Content-Type: application/json' \
        --data-raw '{
        	"username": "username1",
        	"password": "password1"
        }'
        ```
      

    * All Products:
        ```
        curl --location --request GET 'http://localhost:8080/products' \
        --header 'Authorization: Bearer ${YOUR_ACCESS_TOKEN}'
        ```
    * Get Product By ID:
        ```
        curl --location --request GET 'http://localhost:8080/products/{UUID}' \
        --header 'Authorization: Bearer ${YOUR_ACCESS_TOKEN}'
        ```
    * Create Product:
        ```
        curl --location --request POST 'http://localhost:8080/products' \
        --header 'Content-Type: application/json' \
        --header 'Authorization: Bearer ${YOUR_ACCESS_TOKEN}' \
        --data-raw '{
            "name": "Your Product Name",
            "stock": 30.0
        }'
        ```
    * Update Product:
        ```
        curl --location --request PUT 'http://localhost:8080/products/{UUID}' \
        --header 'Content-Type: application/json' \
        --header 'Authorization: Bearer ${YOUR_ACCESS_TOKEN}' \
        --data-raw '{
            "name": "Your New Product Name",
            "stock": 32.0
        }'
        ```
    * Delete Product:
        ```
        curl --location --request DELETE 'http://localhost:8080/products/{UUID}' \
        --header 'Content-Type: application/json' \
        --header 'Authorization: Bearer ${YOUR_ACCESS_TOKEN}'
        ```
    * Trace:
        ```
        curl --location --request GET 'http://localhost:8080/trace'
        ```
    * Websocket:
        Add `ws://127.0.0.1:8080/trace/websocket` to [Websocket Client](https://www.websocket.org/echo.html)