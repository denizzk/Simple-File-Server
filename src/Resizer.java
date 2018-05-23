import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Resizer implements Callable<BufferedImage>{
    private Image originalImage;
    private int scaledWidth;
    private int scaledHeight;
    private boolean preserveAlpha;

    private BufferedImage scaledBI;

    public Resizer(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        this.originalImage = originalImage;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
        this.preserveAlpha = preserveAlpha;
    }

    @Override
    public BufferedImage call() {
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

    public BufferedImage getScaledBI() {
        return scaledBI;
    }
}
