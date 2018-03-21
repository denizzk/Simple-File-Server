import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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

public class ServerTest extends AbstractHandler {
	public void handle(String string, Request rqst, HttpServletRequest request, HttpServletResponse response) {

		int width = Integer.parseInt(request.getParameter("width"));
		int height = Integer.parseInt(request.getParameter("height"));
		String color = request.getParameter("color");

		BufferedImage bufferedImage = null;

		response.setHeader("Content-Type", "image/jpg");

		String fileName = "./img/bears.jpg";

		try {
			String scaledImagePath = scale(fileName, width, height, "newBears.jpg");

			if (width != 0 || height != 0)
				scaledImagePath = scale(fileName, width, height, "scaledBears.jpg");

			if (color != null) {
				RGBDisplayModel rgb = new RGBDisplayModel();
				rgb.setOriginalImage(ImageIO.read(new File(scaledImagePath)));
				switch (color) {
				case "gray":
					scaledImagePath = rgb.getGrayImage("grayBears.jpg");
					break;
				case "red":
					scaledImagePath = rgb.getRedImage("redBears.jpg");
					break;
				case "blue":
					scaledImagePath = rgb.getBlueImage("blueBears.jpg");
					break;
				case "green":
					scaledImagePath = rgb.getGreenImage("greenBears.jpg");
					break;
				}
			}
			FileInputStream fis = new FileInputStream(scaledImagePath);

			sendFile(fis, response.getOutputStream());
			bufferedImage = ImageIO.read(new File(scaledImagePath));
			ImageIO.write(bufferedImage, "jpg", response.getOutputStream());

		} catch (Exception e) {
			Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, e);
		}

	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.setHandler(new ServerTest());
		server.start();
		server.join();
	}

	public String scale(String path, int width, int height, String filename) throws Exception {
		BufferedImage org = ImageIO.read(new File(path));
		BufferedImage resizedCopy = createResizedCopy(org, width, height, true);
		File tosave = new File("img\\" + filename);
		ImageIO.write(resizedCopy, "jpg", tosave);
		return tosave.getAbsolutePath();

	}

	private BufferedImage createResizedCopy(Image org, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
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

	public static void sendFile(FileInputStream fin, OutputStream out) throws Exception {
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = fin.read(buffer)) != -1)
			out.write(buffer, 0, bytesRead);
		fin.close();
	}

	public static String greyFilter(BufferedImage coloredImage, String filename) throws Exception {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		BufferedImage image = op.filter(coloredImage, null);

		File tosave = new File("img\\" + filename);
		ImageIO.write(image, "jpg", tosave);
		return tosave.getAbsolutePath();
	}
}

class RGBDisplayModel {

	private BufferedImage originalImage;
	private BufferedImage redImage;
	private BufferedImage greenImage;
	private BufferedImage blueImage;

	public BufferedImage getOriginalImage() {
		return originalImage;
	}

	public void setOriginalImage(BufferedImage originalImage) {
		this.originalImage = originalImage;
		this.redImage = createColorImage(originalImage, 0xFFFF0000);
		this.greenImage = createColorImage(originalImage, 0xFF00FF00);
		this.blueImage = createColorImage(originalImage, 0xFF0000FF);
	}

	public String getGrayImage(String filename) throws Exception {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		BufferedImage image = op.filter(originalImage, null);

		File tosave = new File("img/" + filename);
		ImageIO.write(image, "jpg", tosave);
		return tosave.getAbsolutePath();
	}

	public String getRedImage(String filename) throws Exception {
		File tosave = new File("img/" + filename);
		ImageIO.write(redImage, "jpg", tosave);
		return tosave.getAbsolutePath();
	}

	public String getGreenImage(String filename) throws Exception {
		File tosave = new File("img/" + filename);
		ImageIO.write(greenImage, "jpg", tosave);
		return tosave.getAbsolutePath();
	}

	public String getBlueImage(String filename) throws Exception {
		File tosave = new File("img/" + filename);
		ImageIO.write(blueImage, "jpg", tosave);
		return tosave.getAbsolutePath();
	}

	private BufferedImage createColorImage(BufferedImage originalImage, int mask) {
		BufferedImage colorImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
				originalImage.getType());

		for (int x = 0; x < originalImage.getWidth(); x++) {
			for (int y = 0; y < originalImage.getHeight(); y++) {
				int pixel = originalImage.getRGB(x, y) & mask;
				colorImage.setRGB(x, y, pixel);
			}
		}

		return colorImage;
	}
}
