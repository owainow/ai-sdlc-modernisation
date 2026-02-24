targetScope = 'subscription'

@description('The environment name (dev, staging, prod)')
param environmentName string

@description('The Azure region for all resources')
param location string = 'uksouth'

@description('Resource group name')
param resourceGroupName string = 'rg-billing-${environmentName}'

@secure()
@description('PostgreSQL administrator password')
param postgresAdminPassword string

resource rg 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
}

module monitoring 'modules/monitoring.bicep' = {
  scope: rg
  name: 'monitoring'
  params: {
    environmentName: environmentName
    location: location
  }
}

module keyvault 'modules/keyvault.bicep' = {
  scope: rg
  name: 'keyvault'
  params: {
    environmentName: environmentName
    location: location
  }
}

module postgresql 'modules/postgresql.bicep' = {
  scope: rg
  name: 'postgresql'
  params: {
    environmentName: environmentName
    location: location
    keyVaultName: keyvault.outputs.keyVaultName
    adminPassword: postgresAdminPassword
  }
}

module redis 'modules/redis.bicep' = {
  scope: rg
  name: 'redis'
  params: {
    environmentName: environmentName
    location: location
  }
}

module containerApps 'modules/container-apps.bicep' = {
  scope: rg
  name: 'container-apps'
  params: {
    environmentName: environmentName
    location: location
    appInsightsConnectionString: monitoring.outputs.appInsightsConnectionString
    postgresqlFqdn: postgresql.outputs.serverFqdn
    redisHostName: redis.outputs.redisHostName
  }
}

output resourceGroupName string = rg.name
output containerAppsEnvironmentId string = containerApps.outputs.environmentId
