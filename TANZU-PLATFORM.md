# Tanzu Platform Deployment Commands

## Create Services

```
cf create-service p.config-server standard config-server -c '{"git": { "uri": "https://github.com/ryanjbaxter/spring-survey-app", "label": "config"} }'

cf create-service postgres on-demand-postgres-db postgres -w

cf create-service p.service-registry standard service-registry

cf create-service p.rabbitmq on-demand-plan rabbit 

cf create-service p.gateway standard gateway

cf create-service p-identity uaa sso
```

## Deploy Apps

```
cd poll-service
../mvnw clean package && cf push
```
```
cd results-service
../mvnw clean package && cf push
```

```
cd poll-ui-js
../mvnw clean package && cf push
```

## Configure Gateway

```
cf bind-service results-service gateway -c '{ "routes": [ { "path": "/results-service/**" } ] }'
cf bind-service poll-service gateway -c '{ "routes": [ { "path": "/poll-service/**" } ] }' 
cf bind-service poll-ui-js gateway -c '{ "routes": [ { "path": "/**", "order": 999, "filters": [ "StripPrefix=0" ] }, { "path": "/results", "filters": [ "StripPrefix=0" ], "sso-enabled": true } ] }'
```