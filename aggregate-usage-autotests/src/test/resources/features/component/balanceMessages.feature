Feature: The aggregate usage of a customer can be retrieved

  Scenario: A customer with unlimited data and unlimited voice calls the service
    Given there is a customer with unlimited data and unlimited voice
    When the client send a text message to the aggregate usage service
    Then the service send a text message containing "je hebt vandaag nog 1.23 GB data"

  Scenario: A customer with data and voice calls the service
    Given there is a customer with data and voice
    When the client send a text message to the aggregate usage service
    Then the service send a text message containing "je hebt deze maand nog 999 MB data"


