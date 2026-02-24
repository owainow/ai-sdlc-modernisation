@description('Environment name')
param environmentName string

@description('Location for resources')
param location string

@description('Administrator login')
param adminLogin string = 'pgadmin'

@secure()
@description('Administrator password')
param adminPassword string

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-12-01-preview' = {
  name: 'psql-billing-${environmentName}'
  location: location
  sku: {
    name: environmentName == 'prod' ? 'Standard_D4ds_v5' : 'Standard_B1ms'
    tier: environmentName == 'prod' ? 'GeneralPurpose' : 'Burstable'
  }
  properties: {
    version: '16'
    administratorLogin: adminLogin
    administratorLoginPassword: adminPassword
    storage: {
      storageSizeGB: environmentName == 'prod' ? 128 : 32
    }
    backup: {
      backupRetentionDays: environmentName == 'prod' ? 35 : 7
      geoRedundantBackup: environmentName == 'prod' ? 'Enabled' : 'Disabled'
    }
    highAvailability: {
      mode: environmentName == 'prod' ? 'ZoneRedundant' : 'Disabled'
    }
  }
}

var databases = ['userdb', 'customerdb', 'billingdb', 'reportingdb']

resource dbs 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-12-01-preview' = [for db in databases: {
  parent: postgresServer
  name: db
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}]

resource firewallRule 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-12-01-preview' = {
  parent: postgresServer
  name: 'AllowAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

output serverFqdn string = postgresServer.properties.fullyQualifiedDomainName
output serverName string = postgresServer.name
