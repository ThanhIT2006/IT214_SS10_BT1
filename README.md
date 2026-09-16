# ProductServiceClientRT - Report

This README documents: the three bugs in the original ProductServiceClientRT, the fixes applied, and how to run unit tests.

1) Three issues from original code
- Using a plain RestTemplate instance (no @LoadBalanced): prevents service-id resolution via Eureka or Spring Cloud LoadBalancer. In production, requests to `http://product-service` would fail to resolve and client would attempt to talk to a hardcoded IP or fail.
- Hardcoded IP:port instead of service-id: this couples client to a specific host and prevents load-balancing across multiple instances; when instances change (staging/production), connection fails or hits wrong host.
- No timeouts configured: blocking network calls may hang worker threads indefinitely under network issues, causing thread-pool exhaustion and reduced availability.

2) Fixes applied
- Created a RestTemplate bean annotated with @LoadBalanced so service-id like `http://product-service` resolves through Spring Cloud LoadBalancer/Eureka.
- Configured timeouts: connectTimeout = 2000ms, readTimeout = 3000ms.
- Replaced hardcoded URL with service-id URL: `http://product-service/api/products/{id}`.
- Added explicit error handling in ProductServiceClientRT:
  - ResourceAccessException (typically timeouts) -> return fallback ProductInfo
  - HttpClientErrorException.NotFound (404) -> throw ProductNotFoundException
  - Other RestClientException -> fallback

3) Files added/changed
- build.gradle: added spring-boot-starter-web and spring-cloud-starter-loadbalancer and BOM
- src/main/java/com/example/bt1/config/RestTemplateConfig.java: RestTemplate @LoadBalanced bean with timeouts
- src/main/java/com/example/bt1/client/ProductServiceClientRT.java: client implementation with error handling and service-id URL
- src/main/java/com/example/bt1/client/ProductInfo.java: simple DTO
- src/main/java/com/example/bt1/exception/ProductNotFoundException.java
- src/test/java/com/example/bt1/client/ProductServiceClientRTTest.java: two unit tests (success + timeout fallback)

4) Run tests
From project root on Windows:

    gradlew.bat test

Or if Gradle wrapper isn't present, use an installed gradle:

    gradle test

Tests included:
- testGetById_success: happy path returns ProductInfo from RestTemplate
- testGetById_timeout_returnsFallback: simulates ResourceAccessException and verifies fallback is returned

5) Notes
- The @LoadBalanced RestTemplate requires Spring Cloud load-balancer on the classpath. In integration with Eureka, ensure proper Spring Cloud dependencies and bootstrap configuration (application.yml) are in place for service discovery.

