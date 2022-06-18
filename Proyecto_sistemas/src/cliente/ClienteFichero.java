/**
 * Javier Abell�n. 18 Mar 2006
 * 
 * Programa de ejemplo de como transmitir un fichero por un socket.
 * Esta es el main con el cliente, que piede un fichero, lo muestra en
 * pantalla y lo escribe cambiando el nombre.
 */

package cliente;

import mensaje.MensajeDameFichero;
import mensaje.MensajeTomaFichero;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ClienteFichero
{
    
    public static void main(String[] args)
    {
        Scanner sn = new Scanner(System.in);
        sn.useDelimiter("\n");
        String nombre;
        ClienteFichero cf = new ClienteFichero();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("C:\\Users\\migag\\OneDrive\\Escritorio\\Proyecto_sistemas\\src\\server\\origen/"))) {
            System.out.println("Estos son los archivos disponibles");
            for (Path file: stream) {
                System.out.println("     -"+file.getFileName());
            }
        }catch(IOException | DirectoryIteratorException ex){
            System.err.println(ex);
        }
        
        System.out.println("\nIngresa el nombre del archivo con su formato (nombre.txt)");
        nombre=sn.next();
        cf.pide("C:\\Users\\migag\\OneDrive\\Escritorio\\Proyecto_sistemas\\src\\server\\origen/"+nombre, "192.168.1.1", 35557, nombre);
    }
    
    
    /**
     * Establece comunicaci�n con el servidor en el puerto indicado. Pide el
     * fichero. Cuando llega, lo escribe en pantalla y en disco duro.
     * 
     * @param fichero
     *            path completo del fichero que se quiere
     * @param servidor
     *            host donde est� el servidor
     * @param puerto
     *            Puerto de conexi�n
     */
    public void pide(String fichero, String servidor, int puerto, String nombre)
    {
        try
        {
            // Se abre el socket.
            Socket socket = new Socket(servidor, puerto);

            // Se env�a un mensaje de petici�n de fichero.
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            MensajeDameFichero mensaje = new MensajeDameFichero();
            mensaje.nombreFichero = fichero;
            oos.writeObject(mensaje);

            // Se abre un fichero para empezar a copiar lo que se reciba.
            FileOutputStream fos = new FileOutputStream("C:\\Users\\migag\\OneDrive\\Escritorio\\Proyecto_sistemas\\src\\cliente\\destinatario/"+nombre);

            // Se crea un ObjectInputStream del socket para leer los mensajes
            // que contienen el fichero.
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            MensajeTomaFichero mensajeRecibido;
            Object mensajeAux;
            do
            {
                // Se lee el mensaje en una variabla auxiliar
                mensajeAux = ois.readObject();
                
                // Si es del tipo esperado, se trata
                if (mensajeAux instanceof MensajeTomaFichero)
                {
                    mensajeRecibido = (MensajeTomaFichero) mensajeAux;
                    // Se escribe en pantalla y en el fichero    
                    fos.write(mensajeRecibido.contenidoFichero, 0,mensajeRecibido.bytesValidos);
                } else
                {
                    // Si no es del tipo esperado, se marca error y se termina
                    // el bucle
                    System.err.println("Mensaje no esperado "+ mensajeAux.getClass().getName());
                    break;
                }
            } while (!mensajeRecibido.ultimoMensaje);
            System.out.println("Se recibio el archivo correctamente");
            // Se cierra socket y fichero
            fos.close();
            ois.close();
            socket.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
