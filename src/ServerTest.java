import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ServerTest extends AbstractHandler
{
    public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response){


        int width = Integer.parseInt(request.getParameter("width"));
        int height = Integer.parseInt(request.getParameter("height"));
        String color = request.getParameter("color");

        BufferedImage bufferedImage = null;

        response.setHeader("Content-Type", "image/jpg");

        String fileName = "./" + request.getPathInfo();

        try {
            String[] nfn = fileName.split("/");
            String scaledImagePath = scale(fileName, width, height, "scaled" + nfn[nfn.length - 1]);

            FileInputStream fis = new FileInputStream(scaledImagePath);

            sendFile(fis, response.getOutputStream());
            bufferedImage = ImageIO.read(new File(scaledImagePath));
            ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

        } catch (Exception e) {
            Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, e);
        }

    }


    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ServerTest());
        server.start();
        server.join();
    }


    public String scale(String path, int width, int height, String filename) throws Exception{
        BufferedImage org = ImageIO.read(new File(path));
        BufferedImage resizedCopy = createResizedCopy(org, width, height, true);
        File tosave = new File("img/" + filename);
        ImageIO.write(resizedCopy, "jpg", tosave);
        return tosave.getAbsolutePath();

    }

    private BufferedImage createResizedCopy(Image org, int scaledWidth, int scaledHeight, boolean preserveAlpha){
        System.out.println("resizing...");
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha)
            g.setComposite(AlphaComposite.Src);
        g.drawImage(org, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public static void sendFile(FileInputStream fin, OutputStream out)throws Exception{
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1)
            out.write(buffer, 0, bytesRead);
        fin.close();
    }


}
