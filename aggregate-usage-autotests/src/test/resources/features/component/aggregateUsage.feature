Feature: The aggregate usage of a customer can be retrieved

  Scenario: A customer with unlimited data and unlimited voice calls the service
    Given there is a customer with unlimited data and unlimited voice
    When the client calls the aggregate usage service
    Then the service responds with the correct values for unlimited data and unlimited voice
    And the service responds with the correct rest of world bundles

  Scenario: A customer with data and voice calls the service
    Given there is a customer with data and voice
    When the client calls the aggregate usage service
    Then the service responds with the correct values for data and voice
    And the service responds with the correct rest of world bundles

  Scenario: A Customer with data and voice and 24UL bundle calls the service
    Given there is a customer with data and voice and 24UL
    When the client calls the aggregate usage service
    Then the service responds with the correct values for data and voice
    And the 24UL bundle is the first bundle in the list
    And the response contains FUP


