import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ServerTest extends AbstractHandler{
    private AsyncHttpClient asyncHttpClient;

    public ServerTest(){
        asyncHttpClient = asyncHttpClient();
        
    }

    public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String color = request.getParameter("color");

        String fileName = "." + request.getPathInfo();
        String scaledImagePath = fileName;
        String[] nfn = fileName.split("/");

        if (!(new File(fileName).exists())) {
            Future<Response> whenResponse = asyncHttpClient.prepareGet("http://bihap.com" + fileName).execute();
            try {
                Files.copy(whenResponse.get().getResponseBodyAsStream(), Paths.get(fileName));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        Scalar scalar = new Scalar();

        if (width != null || height != null)
            scaledImagePath = scalar.scale(fileName, Integer.parseInt(width), Integer.parseInt(height), nfn[nfn.length - 1]);

        if (color != null && color.equals("gray"))
            scaledImagePath = scalar.toGray(scaledImagePath);


        BufferedImage bufferedImage;
        bufferedImage = ImageIO.read(new File(scaledImagePath));
        ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

    }

    public static void main(String[] args) throws Exception {

        QueuedThreadPool threadPool = new QueuedThreadPool(9);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[]{serverConnector});


        //Server server = new Server(8080);
        server.setHandler(new ServerTest());
        server.start();
        server.join();
    }

/*
    private static void sendFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1)
            out.write(buffer, 0, bytesRead);
        in.close();
    }
*/
}