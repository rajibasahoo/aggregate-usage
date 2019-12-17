@Library('jenkins-pipeline-library') _

node {
    deliveryPipeline('springboot') {
        serviceName = 'aggregate-usage'
        scmProject = 'fez'
        scmRepository = 'aggregate-usage'
        buildCommand = 'mvn clean package'
        sonarCommand = 'mvn sonar:sonar'
        network = 'lan'
        imageRepository = 'development/fez-aggregate-usage'
        composeParams = [
                SERVICE_NAME: serviceName,
                REPOSITORY_NAME: imageRepository,
                TIP_STUB_NAME: 'wiremock-tip-aggregate-usage',
                TEAM_NAME: 'Elon',
                CONSUMERS: 'newapp, new mt2Web'
        ]
        autoTests = [
                dir: 'aggregate-usage-autotests',
                command: 'mvn verify -PcucumberTests -Dspring.profiles.active=tst',
                reportDir: 'target/site/cucumber-reports',
                reportFiles: 'feature-overview.html'
        ]
        performanceTests = [
                dir: 'aggregate-usage-loadtests',
                command: 'mvn gatling:test'
        ]
        securityTests = [
                endpoints : [
                        '/health'
                ]
        ]
        pushDockerImage = false
        envToPromoteOnFeature = ['dev', 'tst']
        envToPromoteOnDevelop = ['dev', 'int', 'prf', 'uat']
        envToPromoteOnMaster = []
    }
}