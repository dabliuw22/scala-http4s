# Http4s API

In this project an API rest with `http4s`, `cats`, `doobie` was created. 
Using **Type-Classes**.

Requirements:
   * JDK >= 1.8
   * Scala 2.13.x
   * Docker
   * Docker Compose

1. Run App.

2. Test with cURL:
    * Hello Service:
        ```
        curl --location --request GET 'http://localhost:8080/hello/your_name'
        ```
    * All Products:
        ```
        curl --location --request GET 'http://localhost:8080/products'
        ```
    * Get Product By ID:
        ```
        curl --location --request GET 'http://localhost:8080/products/1'
        ```