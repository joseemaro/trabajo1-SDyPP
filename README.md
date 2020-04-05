## Trabajo Práctico I : Conceptos básicos para la construcción de Sistemas Distribuidos
### Sistemas Distribuidos y Programación Paralela - Universidad Nacional de Luján - 2020

#### Punto 1 y 2
Lo que se puede observar en el segundo punto, es que el agregar una serie de hilos que manejen las peticiones de forma paralela, permite que se reduzcan los tiempos de demora al atender a todos los clientes, ya que van a poder ser atendidos de a más de 1 por vez. En cambio, en el caso del punto 1, donde se atienden de forma secuencial, es solo cuando un cliente deja de comunicarse con el servidor, que otro cliente puede realizar una petición.

Para dejar más en claro esto último, supongamos que la tarea demanda un total de 10 segundos para el cliente 1, y 5 segundos para el cliente 2. En el caso del punto 1, el tiempo total hasta la finalización de la tarea será de 15 segundos, ya que primero atiende a un cliente y luego al otro. En el caso del punto 2, los 2 clientes van a ser atendidos en simultáneo, por lo que el tiempo de finalización de todas las tareas será de 10 segundos.

#### Punto 6
En el programa se puede observar que el pasaje de parámetros en RMI es por valor y no por referencia. 

Para realizar tal comprobación, un vector de enteros fue enviado desde el cliente hacia el servidor. En este último, se inicializó el vector pasado como parámetro, de manera que todos sus campos queden con el valor 0.

Por otro lado, cuando el servidor devuelve el vector al cliente, se puede observar que los valores no cambiaron respecto a los que el cliente previamente envió. Esto demuestra que el pasaje de parámetros es por valor y no por referencia, debido a que, de lo contrario, los valores del vector que recibe el cliente deberían ser aquellos que "sobrescribió" el servidor.
