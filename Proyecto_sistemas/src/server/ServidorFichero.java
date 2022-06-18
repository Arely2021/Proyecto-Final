/**
 * Javier Abell�n, 18 Mar 2006
 * 
 * Programa de ejemplo de como transmitir un fichero por un socket.
 * Esta es la parte del servidor.
 */
package server;

import mensaje.MensajeDameFichero;
import mensaje.MensajeTomaFichero;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clases servidora que env�a un fichero al primer cliente que se lo pida.
 * 
 * @author Javier Abell�n
 */
public class ServidorFichero
{
    /**
     * Instancia la clase servidora y la pone a la escucha del puerto 35557
     * 
     * @param args
     *            de la l�nea de comandos.
     */
    public static void main(String[] args)
    {
        ServidorFichero sf = new ServidorFichero();
        System.out.println("El sevidor esta escuchando");
        sf.escucha(35557);
    }

    /**
     * Se escucha el puerto indicado en espera de clientes a los que enviar
     * el fichero.
     * 
     * @param puerto El puerto de escucha
     */
    public void escucha(int puerto)
    {
        try
        {
            // Se abre el socket servidor
            ServerSocket socketServidor = new ServerSocket(puerto);

            while(true){
                // Se espera un cliente
                Socket cliente = socketServidor.accept();
                Hilo cc = new Hilo(cliente);  //Parametros, la conexion , y los objetos de escritura/lectura
                cc.start();
            }
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Env�a el fichero indicado a trav�s del ObjectOutputStream indicado.
     * @param fichero  Nombre de fichero
     * @param oos ObjectOutputStream por el que enviar el fichero
     */
    
}

class Hilo extends Thread{
    
    Socket cliente = null;

    public Hilo(Socket cliente){  //Constructor
        this.cliente = cliente;
    }
    
    
    private void enviaFichero(String fichero, ObjectOutputStream oos)
    {
        try
        {
            boolean enviadoUltimo=false;
            // Se abre el fichero.
            FileInputStream fis = new FileInputStream(fichero);
            
            // Se instancia y rellena un mensaje de envio de fichero
            MensajeTomaFichero mensaje = new MensajeTomaFichero();
            mensaje.nombreFichero = fichero;
            
            // Se leen los primeros bytes del fichero en un campo del mensaje
            int leidos = fis.read(mensaje.contenidoFichero);
            
            // Bucle mientras se vayan leyendo datos del fichero
            while (leidos > -1)
            {
                
                // Se rellena el n�mero de bytes leidos
                mensaje.bytesValidos = leidos;
                
                // Si no se han leido el m�ximo de bytes, es porque el fichero
                // se ha acabado y este es el �ltimo mensaje
                if (leidos < MensajeTomaFichero.LONGITUD_MAXIMA)
                {
                    mensaje.ultimoMensaje = true;
                    enviadoUltimo=true;
                }
                else
                    mensaje.ultimoMensaje = false;
                
                // Se env�a por el socket
                oos.writeObject(mensaje);
                
                // Si es el �ltimo mensaje, salimos del bucle.
                if (mensaje.ultimoMensaje)
                    break;
                
                // Se crea un nuevo mensaje
                mensaje = new MensajeTomaFichero();
                mensaje.nombreFichero = fichero;
                
                // y se leen sus bytes.
                leidos = fis.read(mensaje.contenidoFichero);
                System.out.println("Enviando archivo");
            }
            
            if (enviadoUltimo==false)
            {
                mensaje.ultimoMensaje=true;
                mensaje.bytesValidos=0;
                oos.writeObject(mensaje);
            }
            System.out.println("Archivo enviado \n");
            // Se cierra el ObjectOutputStream
            oos.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    
    @Override
    public void run(){
        System.out.println("Aceptado cliente");

        try {
            // Cuando se cierre el socket, esta opci�n hara que el cierre se
            // retarde autom�ticamente hasta 10 segundos dando tiempo al cliente
            // a leer los datos.
            cliente.setSoLinger(true, 10);
      
            // Se lee el mensaje de petici�n de fichero del cliente.
            ObjectInputStream ois = new ObjectInputStream(cliente.getInputStream());
            Object mensaje = ois.readObject();
            
            // Si el mensaje es de petici�n de fichero
            if (mensaje instanceof MensajeDameFichero)
            {
                // Se muestra en pantalla el fichero pedido y se envia
                System.out.println("Me piden: " + ((MensajeDameFichero) mensaje).nombreFichero);
                enviaFichero(((MensajeDameFichero) mensaje).nombreFichero,new ObjectOutputStream(cliente.getOutputStream()));
            }
            else
            {
                // Si no es el mensaje esperado, se avisa y se sale todo.
                System.err.println (
                        "Mensaje no esperado "+mensaje.getClass().getName());
            }
            
            // Cierre de sockets
          } catch (SocketException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Hilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}