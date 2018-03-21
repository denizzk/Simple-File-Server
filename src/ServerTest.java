import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class ServerTest extends AbstractHandler {

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		// response.setCharacterEncoding("iso-8859-9");
		// response.getWriter().println("<h1>SA World</h1>");
		// Enumeration<String> parameterNames = request.getParameterNames();
		// while (parameterNames.hasMoreElements()) {
		// String name = parameterNames.nextElement();
		// String value = request.getParameter(name).toString();
		// response.getWriter().write(String.format("%s:%s<br>", name, value));
		// }
		// response.getWriter().write(String.format("<br>Method:%s<br>",
		// request.getMethod()));
		// response.getWriter().write(String.format("Request URI:%s<br>",
		// request.getRequestURI()));
		// response.getWriter().write(String.format("Context:%s<br>",
		// request.getContextPath()));
		// response.getWriter().write(String.format("Path:%s<br>",
		// request.getPathInfo()));
		// response.getWriter().write(String.format("Local Host:%s<br>",
		// request.getLocalName()));
		// response.getWriter().write(String.format("Remote Host:%s<br>",
		// request.getRemoteHost()));
		// response.setStatus(HttpStatus.BAD_GATEWAY_502);
		// baseRequest.setHandled(true);
		// response.sendRedirect("http://www.google.com");
		
		
		BufferedImage bufferedImage = null;		
		FileInputStream bis = new FileInputStream("C:\\Users\\Deniz\\Documents\\GitHub\\Simple-File-Server\\img\\bears.jpg");

		try {
			sendFile(bis, response.getOutputStream());
			bufferedImage = ImageIO.read(new File("./img/bears.jpg"));
			response.setHeader("Content-Type", "image/jpg");
			ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
		} catch (Exception e) {
			Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static void sendFile(FileInputStream fis, ServletOutputStream outputStream) throws Exception {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = fis.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		fis.close();
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		// ContextHandler context = new ContextHandler();
		// context.setContextPath("/path");
		// context.setHandler(new ServerTest());
		server.setHandler(new ServerTest());
		// server.setHandler(context);
		server.start();
		server.join();
	}

	public String scale(String path, int width, int height, String filename) throws Exception {
		BufferedImage originalImage = ImageIO.read(new File(path));
		BufferedImage resizedCopy = createResizedCopy(originalImage, width, height, true);
		File toSave = new File("img\\" + filename);
		ImageIO.write(resizedCopy, "jpg", toSave);
		return toSave.getAbsolutePath();
	}

	private BufferedImage createResizedCopy(BufferedImage originalImage, int scaledWidth, int scaledHeight,
			boolean preserveAlpha) {
		System.out.println("resizing...");
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha)
			g.setComposite(AlphaComposite.Src);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose();
		return scaledBI;
	}
}
