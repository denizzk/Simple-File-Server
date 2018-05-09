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
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ServerTest extends AbstractHandler{
    private AsyncHttpClient asyncHttpClient;

    private ConcurrentMap<String, BufferedImage> cache;

    public ServerTest(){
        asyncHttpClient = asyncHttpClient();
        cache = new ConcurrentHashMap<String, BufferedImage>();
    }

    public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String color = request.getParameter("color");


        String[] nfn = request.getPathInfo().split("/");
        String fileName = nfn[nfn.length - 1];

        BufferedImage bufferedImage;

        if (!(cache.containsKey(fileName))) {
            Future<Response> whenResponse = asyncHttpClient.prepareGet("http://bihap.com/img/" + fileName).execute();
            try {
                cache.put(fileName, ImageIO.read(whenResponse.get().getResponseBodyAsStream()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        bufferedImage = cache.get(fileName);

        if (width != null || height != null)
            bufferedImage = resize(bufferedImage, Integer.parseInt(width), Integer.parseInt(height), true);

        if (color != null && color.equals("gray"))
            bufferedImage = toGray(bufferedImage);

        ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

    }

    public static void main(String[] args) throws Exception {

        QueuedThreadPool threadPool = new QueuedThreadPool(12, 4,1000000000);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[]{serverConnector});

        //Server server = new Server(8080);
        server.setHandler(new ServerTest());
        server.start();
        server.join();
    }

    private static BufferedImage resize(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        System.out.println("resizing...");
        int imageType = preserveAlpha ? 1 : 2;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }

        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    private static BufferedImage toGray(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImg = new BufferedImage(width, height, 2);

        for(int i = 0; i < height; ++i) {
            for(int j = 0; j < width; ++j) {
                Color c = new Color(originalImage.getRGB(j, i));
                int red = (int)((double)c.getRed() * 0.299D);
                int green = (int)((double)c.getGreen() * 0.587D);
                int blue = (int)((double)c.getBlue() * 0.114D);
                Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);
                newImg.setRGB(j, i, newColor.getRGB());
            }
        }

        return newImg;
    }

}