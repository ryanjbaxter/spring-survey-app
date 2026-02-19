# Tanzu Platform Deployment Commands

## Create Services

```
cf create-service p.config-server standard config-server -c '{"git": { "uri": "https://github.com/ryanjbaxter/spring-survey-app", "label": "config"} }'
```

TODO: Add other services

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
cf bind-service poll-ui-js gateway -c '{ "routes": [ { "path": "/**", "order": 999, "filters": [ "StripPrefix=0" ] } ] }'
```