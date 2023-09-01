import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Multithreaded simple web server implementation
 */
public class WebServer {

    public static final String DOCUMENT_ROOT = "Resources/";
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {

            WebServer webServer = new WebServer();
            webServer.listen();
    }

    private void listen() {

        try {

            ServerSocket bindSocket = new ServerSocket(DEFAULT_PORT);
            serve(bindSocket);

        } catch (IOException e) {

            System.exit(1);

        }
    }

    private void serve(ServerSocket bindSocket) {

        while (true) {

            try {

                Socket clientSocket = bindSocket.accept();

                Thread threadDispatch = new Thread(new WebServerDispatch(clientSocket));
                threadDispatch.start();

            } catch (IOException e) {

                System.out.println(e.getMessage());
            }
        }
    }
}
