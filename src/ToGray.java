import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class ToGray implements Callable<BufferedImage>{
    private BufferedImage originalImage;
    private BufferedImage newImg;

    public ToGray(BufferedImage originalImage) {
        this.originalImage = originalImage;
    }

    @Override
    public BufferedImage call() {
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

    public BufferedImage getNewImg() {
        return newImg;
    }
}
