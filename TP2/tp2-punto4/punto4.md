# Punto 4, operador sobel
### Indice
1. Como ejecutarlo.
2. Resultados punto b.
3. Resultados punto c.

1. Primero hay que inciar rabbitMQ, desde windows basta con darle start a la aplicacion.
En el caso de linux se utiliza el siguiente comando:

```sh
rabbitmq-server start
```

Luego se ejecutar en el siguiente orden:
1- Ejecutar ServerSobel, esta clase es la que actua como un dispacher entre el cliente y los workers.
2- Ejecutar SobelClient, este es el que se encarga de llamar a la funcion sobel distribuida y le da una imagen para procesar.

## Notas
_ServerSobel se encarga de la comunicacion via rmi con el cliente. Actua como dispacher enviando las tareas a traves de una cola hacia los workers y 
recibe las imagenes procesadas por los mismos a traves de una cola.
_WorkerSobel: recibe las partes de imagenes a procesar a traves de una cola, las procesa(aplica el proceso sobel) y las devuelve a traves de una cola hacia el server sobel.
_SobelRequest: correlacion entre WorkerSobel y parte de la imagen asignada.


2. En el caso del punto b, se midio un timestamp desde el comienzo del proceso hasta que el cliente recibe la imagen procesada.
Vale aclarar que se compara el tiempo establecido tanto cuando se realiza el sobel de forma local en el cliente, y cuando se realiza de modo disribuido entre los workers,
y se comparan 2 imagenes de distinto tamaño, una de 0,98MB y otra de 31kb.

## Prueba: Resultado(elapsed time).
Local-imagen chica:  611.
Local-imagen Grande: 4875.
Distribuido-2 servers-imagen chica: 1353.
Distribuido-5 servers-imagen chica: 1450.
Distribuido-10 servers-imagen chica: 1481.
Distribuido-2 servers-imagen grande: 10174.
Distribuido-5 servers-imagen grande: 9966.
Distribuido-10 servers-imagen grande: 10092.
Distribuido-100 servers-imagen grande: 12690.

### Conclusion:
En el caso de prueba debido a que el tamaño de las imagenes no eran lo suficientemene pesadas, posiblemente si se implementara sobre imagenes muchos mas pesadas se verian los verdaderos
resultados notorios, en este caso al ser un archivo liviano el que se procesa, se lleva a cabo mas rapido el proceso de forma local que de forma distribuida, ya que se demora mas en levantar las colas y los workers y realizar la comunicacion que en realizar el proceso en si de forma distribuida.
En cuanto a las diferencias entre la cantidad de servidores utilizados se nota algo similar, al ser un archivo pequeño no se requiere de muchos servidores, en cuanto mas servidores se
creen mas se va a tardar en terminar el proceso porque se tarda mas en inicalizar los workers y las colas que en realizar el proceso. 


3. Para el caso c, basicamente lo que se hace es poner un delay y si el servidor(dispacher) no recibe la respuesta en determiado tiempo toma ese worker como caido, y se asigna esa parte de la imagen a otro worker para que lo procese.


## MEJORA
Una mejora podria ser que los workers puedan atender pedidos de otros servidores, no solo este dispacher, de forma que tengan una mayor carga,y que haya mas de un cliente conectado a ese dispacher pidiendo procesar imagenes, de esta forma se entiende un poco mas el porque de hacer toda una estructura tan compleja.
