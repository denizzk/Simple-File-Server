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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.*;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ServerTest extends AbstractHandler{
    private AsyncHttpClient asyncHttpClient;
    private ExecutorService executor;

    private ConcurrentMap<String, BufferedImage> cache;

    public ServerTest(){
        asyncHttpClient = asyncHttpClient();
        cache = new ConcurrentHashMap<String, BufferedImage>();
        executor = Executors.newCachedThreadPool();
    }

    public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String color = request.getParameter("color");

        String[] nfn = request.getPathInfo().split("/");
        String fileName = nfn[nfn.length - 1];

        BufferedImage bufferedImage;
        Future<Response> whenResponse;

        if (!(cache.containsKey(fileName))) {
            whenResponse = asyncHttpClient.prepareGet("http://bihap.com/img/" + fileName).execute();
            try {
                cache.putIfAbsent(fileName, ImageIO.read(whenResponse.get().getResponseBodyAsStream()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        bufferedImage = cache.get(fileName);

        Future<BufferedImage> whenReady;

        if (width != null || height != null){
            Resizer resizer = new Resizer(bufferedImage, Integer.parseInt(width), Integer.parseInt(height), true);
            whenReady = executor.submit(resizer);
            try {
                bufferedImage = whenReady.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }

        if (color != null && color.equals("gray")){
            ToGray toGray = new ToGray(bufferedImage);
            whenReady = executor.submit(toGray);
            try {
                bufferedImage = whenReady.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }

        ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

    }

    public static void main(String[] args) throws Exception {

        QueuedThreadPool threadPool = new QueuedThreadPool(12,4);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[]{serverConnector});

        //Server server = new Server(8080);
        server.setHandler(new ServerTest());
        server.start();
        server.join();
    }

}