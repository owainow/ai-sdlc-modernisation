@description('Environment name')
param environmentName string

@description('Location for resources')
param location string

@description('Application Insights connection string')
param appInsightsConnectionString string

@description('PostgreSQL server FQDN')
param postgresqlFqdn string

@description('Redis host name')
param redisHostName string

@description('ACR login server (e.g., acrbilling.azurecr.io)')
param acrLoginServer string = 'acrbilling.azurecr.io'

@description('Container image tag')
param imageTag string = 'latest'

var services = [
  {
    name: 'user-service'
    dbName: 'userdb'
    port: 8080
    cpu: '0.5'
    memory: '1Gi'
    minReplicas: 1
    maxReplicas: 5
  }
  {
    name: 'customer-service'
    dbName: 'customerdb'
    port: 8080
    cpu: '0.5'
    memory: '1Gi'
    minReplicas: 1
    maxReplicas: 5
  }
  {
    name: 'billing-service'
    dbName: 'billingdb'
    port: 8080
    cpu: '1.0'
    memory: '2Gi'
    minReplicas: 2
    maxReplicas: 10
  }
  {
    name: 'reporting-service'
    dbName: 'reportingdb'
    port: 8080
    cpu: '1.0'
    memory: '2Gi'
    minReplicas: 1
    maxReplicas: 8
  }
]

resource acaEnvironment 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: 'cae-billing-${environmentName}'
  location: location
  properties: {
    zoneRedundant: environmentName == 'prod'
  }
}

resource containerApps 'Microsoft.App/containerApps@2024-03-01' = [for service in services: {
  name: 'ca-${service.name}-${environmentName}'
  location: location
  properties: {
    managedEnvironmentId: acaEnvironment.id
    configuration: {
      ingress: {
        external: true
        targetPort: service.port
        transport: 'http'
      }
    }
    template: {
      containers: [
        {
          name: service.name
          image: '${acrLoginServer}/${service.name}:${imageTag}'
          resources: {
            cpu: json(service.cpu)
            memory: service.memory
          }
          env: [
            { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsightsConnectionString }
            { name: 'SPRING_DATASOURCE_URL', value: 'jdbc:postgresql://${postgresqlFqdn}:5432/${service.dbName}' }
            { name: 'SPRING_REDIS_HOST', value: redisHostName }
          ]
        }
      ]
      scale: {
        minReplicas: service.minReplicas
        maxReplicas: service.maxReplicas
        rules: [
          {
            name: 'http-scale'
            http: {
              metadata: {
                concurrentRequests: '50'
              }
            }
          }
        ]
      }
    }
  }
}]

output environmentId string = acaEnvironment.id
