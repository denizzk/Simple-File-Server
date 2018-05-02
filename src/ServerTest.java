import org.asynchttpclient.AsyncHttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ByteArrayOutputStream2;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.*;
import org.asynchttpclient.*;


public class ServerTest extends AbstractHandler{


    public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String color = request.getParameter("color");

        ConcurrentMap<String, byte[]> images = new ConcurrentHashMap<String, byte[]>();


        String fileName = request.getPathInfo();
        System.out.println("filename:" + fileName);


        if (images.get(fileName) == null) {
            AsyncHttpClient asyncHttpClient = asyncHttpClient();
            Future<Response> whenResponse = asyncHttpClient.prepareGet("http://bihap.com" + fileName).execute();
            try {
                images.putIfAbsent(fileName, whenResponse.get().getResponseBodyAsBytes());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


        if(width == null && height == null && !color.equals("gray")){
             sendFile(new ByteArrayInputStream(images.get(fileName)), response.getOutputStream());
        }

        else {
            BufferedImage bufferedImage;

            ByteArrayInputStream in = new ByteArrayInputStream(images.get(fileName));
            bufferedImage = ImageIO.read(in);

            if (width != null || height != null)
                bufferedImage = resize(bufferedImage, Integer.parseInt(width), Integer.parseInt(height), true);


            if (color != null && color.equals("gray"))
                bufferedImage = toGray(bufferedImage);

            /*
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            sendFile(is, response.getOutputStream());
            */
            ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

        }
    }

    public static void main(String[] args) throws Exception {
/*
        QueuedThreadPool threadPool = new QueuedThreadPool(4);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[]{serverConnector});
*/

        Server server = new Server(8080);
        server.setHandler(new ServerTest());
        server.start();
        server.join();
    }


    private static void sendFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1)
            out.write(buffer, 0, bytesRead);
        in.close();
    }

    private BufferedImage resize(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
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

    private BufferedImage toGray(BufferedImage originalImage) {
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