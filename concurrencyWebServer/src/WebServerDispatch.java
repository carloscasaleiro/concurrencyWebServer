import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Web server client dispatcher
 */
public class WebServerDispatch implements Runnable{

    private static final Logger logger = Logger.getLogger(WebServer.class.getName());

    private final Socket socket;

    public WebServerDispatch(Socket clientSocket) {

        this.socket=clientSocket;
    }


    @Override
    public void run() {

        dispatch(socket);
        System.out.println("Thread: " + Thread.currentThread().getName());

    }

    private void dispatch(Socket clientSocket) {

        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            String requestHeaders = fetchRequestHeaders(in);
            if (requestHeaders.isEmpty()) {
                close(clientSocket);
                return;
            }

            String request = requestHeaders.split("\n")[0]; // request is first line of header
            String httpVerb = request.split(" ")[0]; // verb is the first word of request
            String resource = request.split(" ").length > 1 ? request.split(" ")[1] : null; // second word of request

            logger.log(Level.INFO, "Request received: " + request);
            logger.log(Level.FINE, "Headers : \n" + requestHeaders);


            if (!httpVerb.equals("GET")) {
                logger.log(Level.WARNING, "request not supported from " + getAddress(clientSocket));
                reply(out, HttpHelper.notAllowed());
                close(clientSocket);
                return;

            }

            if (resource == null) {
                logger.log(Level.WARNING, "resource not specified from " + getAddress(clientSocket));
                reply(out, HttpHelper.badRequest());
                close(clientSocket);
                return;
            }

            String filePath = getPathForResource(resource);
            if (!HttpMedia.isSupported(filePath)) {
                logger.log(Level.WARNING, "request for content type not supported from " + getAddress(clientSocket));
                reply(out, HttpHelper.unsupportedMedia());
                close(clientSocket);
                return;
            }

            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {

                reply(out, HttpHelper.ok());

            } else {

                logger.log(Level.WARNING, file.getPath() + " not found");
                reply(out, HttpHelper.notFound());
                filePath = WebServer.DOCUMENT_ROOT + "404.html";
                file = new File(filePath);

            }

            reply(out, HttpHelper.contentType(filePath));
            reply(out, HttpHelper.contentLength(file.length()));

            streamFile(out, file);
            close(clientSocket);

        } catch (SocketException ex) {

            logger.log(Level.INFO, "client disconnected " + getAddress(clientSocket));

        } catch (IOException ex) {

            logger.log(Level.WARNING, ex.getMessage());
            close(clientSocket);
        }
    }

    private String fetchRequestHeaders(BufferedReader in) throws IOException {

        String line;
        StringBuilder builder = new StringBuilder();

        // read the full http request
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            builder.append(line).append("\n");
        }

        return builder.toString();

    }

    private String getPathForResource(String resource) {

        String filePath = resource;

        Pattern pattern = Pattern.compile("(\\.[^.]+)$"); // regex for file extension
        Matcher matcher = pattern.matcher(filePath);

        if (!matcher.find()) {
            filePath += "/index.html";
        }

        filePath = WebServer.DOCUMENT_ROOT + filePath;

        return filePath;
    }

    private void reply(DataOutputStream out, String response) throws IOException {
        out.writeBytes(response);
    }

    private void streamFile(DataOutputStream out, File file) throws IOException {

        byte[] buffer = new byte[1024];
        FileInputStream in = new FileInputStream(file);

        int numBytes;
        while ((numBytes = in.read(buffer)) != -1) {
            out.write(buffer, 0, numBytes);
        }

        in.close();
    }

    private void close(Socket clientSocket) {

        try {

            logger.log(Level.INFO, "closing client socket for " + getAddress(clientSocket));
            clientSocket.close();

        } catch (IOException e) {

            logger.log(Level.INFO, e.getMessage());
        }
    }

    private String getAddress(Socket socket) {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
    }
}
