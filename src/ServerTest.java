import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
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

		response.setCharacterEncoding("iso-8859-9");
		response.getWriter().println("<h1>SA World</h1>");
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			String value = request.getParameter(name).toString();
			response.getWriter().write(String.format("%s:%s<br>", name, value));
		}
		response.getWriter().write(String.format("<br>Method:%s<br>", request.getMethod()));
		response.getWriter().write(String.format("Request URI:%s<br>", request.getRequestURI()));
		response.getWriter().write(String.format("Context:%s<br>", request.getContextPath()));
		response.getWriter().write(String.format("Path:%s<br>", request.getPathInfo()));
		response.getWriter().write(String.format("Local Host:%s<br>", request.getLocalName()));
		response.getWriter().write(String.format("Remote Host:%s<br>", request.getRemoteHost()));
		// response.setStatus(HttpStatus.IM_A_TEAPOT_418);
		baseRequest.setHandled(true);
		// response.sendRedirect("http://www.google.com");

	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		ContextHandler context = new ContextHandler();
		context.setContextPath("/path");
		context.setHandler(new ServerTest());

		server.setHandler(context);
		server.start();
		server.join();
	}
}
