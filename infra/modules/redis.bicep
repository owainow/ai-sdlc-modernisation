@description('Environment name')
param environmentName string

@description('Location for resources')
param location string

resource redis 'Microsoft.Cache/redis@2023-08-01' = {
  name: 'redis-billing-${environmentName}'
  location: location
  properties: {
    sku: {
      name: environmentName == 'prod' ? 'Standard' : 'Basic'
      family: 'C'
      capacity: environmentName == 'prod' ? 2 : 0
    }
    enableNonSslPort: false
    minimumTlsVersion: '1.2'
    redisConfiguration: {
      'maxmemory-policy': 'allkeys-lru'
    }
  }
}

output redisHostName string = redis.properties.hostName
output redisPort int = redis.properties.sslPort
