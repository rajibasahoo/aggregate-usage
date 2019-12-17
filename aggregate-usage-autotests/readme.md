### Running in local/dev environment

To run locally, first you need to have application running. See the README.md of the service how to set this up.

Run following command from aggregate-usage-autotests module:
```
mvn clean verify -PcomponentTests -Dspring.profiles.active=local
```

By default, tests are going to start local WireMock server for each stubbed sub-system.

### Running against service in docker environment

To run tests against service running in docker environment, execute following command

```
mvn clean verify -PcomponentTests -Dspring.profiles.active=tst
```
