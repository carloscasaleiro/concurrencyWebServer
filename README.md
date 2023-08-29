# Concurrency Web Server

The server listens on a specified port (defaulting to 8080) for incoming client connections. Upon connection, it creates a new thread (WebServerDispatch) to handle each client's HTTP requests and responses.

The WebServerDispatch class processes the incoming client requests:

Parses the HTTP request headers to extract the requested resource and HTTP verb (usually GET).
Checks if the requested media type is supported.
Generates appropriate HTTP responses, including status codes and headers like Content-Type and Content-Length.
Streams the content of requested files back to the client using a DataOutputStream and a FileInputStream.

This simple implementation is capable of serving static files (such as HTML) over HTTP.

Project made during the Academia de Código bootcamp between May -> Aug 2023. www.academiadecodigo.org
<p></p>

Run and try on browser:

http://localhost:8080/index.html

http://localhost:8080/logo.png


http://localhost:8080/404.html
