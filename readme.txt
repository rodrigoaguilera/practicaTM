Caracteristicas implementadas

Ernest Pastor
Rodrigo Aguilera


-Es capaz de leer los ficheros zip con imagenes y guardar lo 
leido en un fichero denominado ptm que consiste en un flujo de jpgs 
dentro de un paquete gzip. Tambien es capaz de leer este contenedor y usarlo.

-También es capaz de reproducir y pausar el vídeo de las imágenes aunque 
sin control de sincronia ya que se asume que son video en bucle que no 
tienen que coordinarse con ningun flujo de audio.

-En cuanto a la interfaz es una simple ventana con un marco para 
reproducir video, un menu para hacer las operaciones de archivo y filtrado.

-Solo se ha implementado un filtro que transforma la imagen a escala de grises 
y se considera suficiente para comprender la mecanica de la manipulación 
de imagen pixel a pixel.

-Hay tres clases, 
*la Imagen que almacena la bufferedImage y la posibilidad e ordenarse en una colección.
*El reproductor que se muestra en el marco con el video a reproducir
*la aplicacion principal en la que cada una de las funciones implemente 
una respuesta de accion a una pulsacion en la interfaz


-Se adjunta todo el projecto de netbeans 7.2.1 probado contra el JDK 7 de Java.