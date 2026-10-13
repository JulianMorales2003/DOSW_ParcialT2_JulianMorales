# Despliegue en Azure App Service

Guía para desplegar el servicio en Azure con la imagen publicada en Docker Hub. El pipeline ya incluye el job `deploy`; solo necesita que existan los recursos y los secrets.

> Azure no permitió crear el App Service con la suscripción disponible, por eso esta guía queda como la configuración completa del despliegue.

## Requisitos
- Cuenta de Azure con suscripción activa (p. ej. Azure for Students) y [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli).
- Imagen `usuario/dosw-parcial-t2:latest` ya publicada en Docker Hub (la sube el job `docker`).
- Base MongoDB accesible desde internet (Azure Cosmos DB for MongoDB o MongoDB Atlas).

## 1. Crear los recursos
Reemplazar los valores entre `<>`. El nombre de la app debe ser único en Azure. Si la suscripción restringe regiones o planes, usar una región y un plan que permita.

```bash
az login

az group create --name rg-dosw-parcial --location <region-permitida>

az appservice plan create \
  --name plan-dosw-parcial \
  --resource-group rg-dosw-parcial \
  --is-linux --sku B1

az webapp create \
  --name <nombre-unico-app> \
  --resource-group rg-dosw-parcial \
  --plan plan-dosw-parcial \
  --container-image-name <usuario-dockerhub>/dosw-parcial-t2:latest
```

## 2. Variables de entorno del App Service
```bash
az webapp config appsettings set \
  --name <nombre-unico-app> \
  --resource-group rg-dosw-parcial \
  --settings \
    MONGODB_URI="<cadena-de-conexion>" \
    JWT_SECRET="<secret-base64>" \
    WEBSITES_PORT=8080
```

## 3. Service Principal para el pipeline
```bash
az ad sp create-for-rbac \
  --name dosw-parcial-sp \
  --role contributor \
  --scopes /subscriptions/<subscription-id>/resourceGroups/rg-dosw-parcial \
  --sdk-auth
```
Guardar el JSON completo que devuelve como secret `AZURE_CREDENTIALS` en GitHub.

## 4. Secrets en GitHub
En Settings → Secrets and variables → Actions:

| Secret | Contenido |
|---|---|
| `AZURE_CREDENTIALS` | JSON del Service Principal |
| `AZURE_WEBAPP_NAME` | Nombre del App Service (`<nombre-unico-app>`) |

## 5. Desplegar y verificar
Hacer push a `develop` (o re-ejecutar el workflow). Cuando el pipeline termine en verde:

- `https://<nombre-unico-app>.azurewebsites.net/api/v1/health` responde 200.
- `https://<nombre-unico-app>.azurewebsites.net/swagger-ui/index.html` carga Swagger.

Las URLs reales reemplazan a `<nombre-app>` en la sección 3 del README.