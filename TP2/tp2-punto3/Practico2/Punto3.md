# Punto3 - Balanceador de Carga:

| Estado | Carga  | Acción
| :------------- | :------------- | :-------------
| **GLOBAL_CRITICAL**  | 80% - 100% | Se crean **nodosActivos/2** nuevos nodos (si es mayor a 1, sino 1)**
| **GLOBAL_ALERT** | 50% - 80% | Se crean **nodosActivos/4** nuevos nodos (si es mayor a 1, sino 1)**
| **GLOBAL_NORMAL** | 20% - 50% | -
| **GLOBAL_IDLE** | 0% - 20% | Se eliminan **nodosActivos/3** nodos (si es mayor a 1, sino 1)

*Sumatoria de todas las cargas de cada Nodo.  
**Asignandole todos los servicios existentes.


## Instalar

1. Instalar RabbitMQ 

- Instalar dependencias de java
```sh
mvn install
```

### Ejecutar

1. Definir los parametros de rabbit en `Practico2/src/main/java/punto3/resources/rabbitmq.properties` con sus datos.

- Iniciar servicio de RabbitMQ
```sh
rabbitmq-server start
```

- Ejecutar en el siguiente orden:
```
Dispatcher
NodeMain
ServerMain
ClientGenerator
```

