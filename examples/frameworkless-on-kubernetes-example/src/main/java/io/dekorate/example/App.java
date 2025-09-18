package io.dekorate.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.annotation.Port;

@KubernetesApplication(ports = @Port(name = "http", containerPort = 8080))
public class App {
  public static void main(String[] args) throws IOException {
    int serverPort = 8080;
    HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
    server.createContext("/api/hello", (exchange -> {
      String respText = "Hello From k8s FrameworkLess world!";
      exchange.sendResponseHeaders(200, respText.getBytes().length);
      OutputStream output = exchange.getResponseBody();
      output.write(respText.getBytes());
      output.flush();
      exchange.close();
    }));
    server.setExecutor(null); // creates a default executor
    System.out.println("Listening in port " + serverPort);
    server.start();
  }

}
