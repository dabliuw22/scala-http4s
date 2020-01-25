# Http4s API

In this project an API rest with `http4s`, `cats`, `doobie`, `skunk` was created. 
Using **Type-Classes**.

Requirements:
   * JDK >= 1.8
   * Scala 2.13.x
   * Docker
   * Docker Compose

1. Run App.

2. Test with cURL:
    * All Products:
        ```
        curl --location --request GET 'http://localhost:8080/products'
        ```
    * Get Product By ID:
        ```
        curl --location --request GET 'http://localhost:8080/products/{UUID}'
        ```
    * Create Product:
        ```
        curl --location --request POST 'http://localhost:8080/products' \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "name": "Your Product Name",
            "stock": 30.0
        }'
        ```
      * Update Product:
        ```
        curl --location --request PUT 'http://localhost:8080/products/{UUID}' \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "name": "Your New Product Name",
            "stock": 32.0
        }'
        ```
      * Delete Product:
        ```
        curl --location --request DELETE 'http://localhost:8080/products/{UUID}' \
        --header 'Content-Type: application/json'
        ```