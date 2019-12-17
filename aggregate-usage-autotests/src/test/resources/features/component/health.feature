Feature: The micro-service health-check can be retrieved
  Scenario: Client makes call to /health
    When the client calls /health
    Then the service responds with "UP"


