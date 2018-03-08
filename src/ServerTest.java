import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class ServerTest extends AbstractHandler
{

    @Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException, ServletException
    {

        //response.setCharacterEncoding("iso-2022-jp");

        response.getWriter().write("<h1>Hello World</h1>");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()){
            String name = (String) parameterNames.nextElement();
            String value = request.getParameter(name).toString();
            response.getWriter().write(String.format("%s:%s\n", name, value));
        }

        response.getWriter().write("<br>");
        response.getWriter().write(String.format("Method:%s<br>", request.getMethod()));
        response.getWriter().write(String.format("Request URI:%s<br>", request.getRequestURI()));
        response.getWriter().write(String.format("Context:%s<br>", request.getContextPath()));
        response.getWriter().write(String.format("Path:%s<br>", request.getPathInfo()));
        response.getWriter().write(String.format("Local Host:%s<br>", request.getLocalName()));
        response.getWriter().write(String.format("Remote Host:%s<br>", request.getRemoteHost()));

        //response.sendRedirect("http://google.com");

        //response.setStatus(HttpStatus.FORBIDDEN_403);

        baseRequest.setHandled(true);

    }

    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8080);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/merhabaaa");
        context.setHandler(new ServerTest());
        server.setHandler(context);

        server.start();
        server.join();
    }
}
