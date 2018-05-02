import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Scalar {
    public static String THUMBNAILS = "./img/thumbnails/";

    public Scalar() {
        new File(THUMBNAILS).mkdirs();
    }

    public String scale(String path, int width, int height, String filename) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(path));
        BufferedImage resizedCopy = this.createResizedCopy(originalImage, width, height, true);
        File tosave = new File(THUMBNAILS + filename);
        ImageIO.write(resizedCopy, "jpg", tosave);
        return tosave.getAbsolutePath();
    }

    public String toGray(String path) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(path));
        BufferedImage resizedCopy = this.convertGrayScale(originalImage);
        File tosave = new File(THUMBNAILS + "gray.jpg");
        ImageIO.write(resizedCopy, "jpg", tosave);
        return tosave.getAbsolutePath();
    }

    private BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
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

    private BufferedImage convertGrayScale(BufferedImage originalImage) {
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
