Feature: The national usage of a customer can be retrieved

  Scenario: A customer with unlimited data and unlimited voice calls the service
    Given there is a customer with unlimited data and unlimited voice
    When the client calls the aggregate usage service national endpoint
    Then the service responds with the correct values for unlimited data and unlimited voice

  Scenario: A customer with data and voice calls the service
    Given there is a customer with data and voice
    When the client calls the aggregate usage service national endpoint
    Then the service responds with the correct values for data and voice


