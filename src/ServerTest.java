import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.*;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class ServerTest extends HttpServlet{
    private AsyncHttpClient asyncHttpClient;
    private ExecutorService executor;

    private ConcurrentMap<String, BufferedImage> cache;

    public ServerTest(){
        asyncHttpClient = asyncHttpClient();
        cache = new ConcurrentHashMap<String, BufferedImage>();
        executor = Executors.newCachedThreadPool();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String color = request.getParameter("color");

        String[] nfn = request.getServletPath().split("/");
        String fileName = nfn[nfn.length - 1];


        AsyncContext async = request.startAsync();
        ServletOutputStream out = response.getOutputStream();
        out.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                BufferedImage bufferedImage;
                Future<Response> whenResponse;
                while (out.isReady()){
                    if (!(cache.containsKey(fileName))) {
                        whenResponse = asyncHttpClient.prepareGet("http://bihap.com/img/" + fileName).execute();
                        try {
                            cache.putIfAbsent(fileName, ImageIO.read(whenResponse.get().getResponseBodyAsStream()));
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    bufferedImage = cache.get(fileName);

                    Future<BufferedImage> whenReady;

                    if (width != null || height != null){
                        Resizer resizer = new Resizer(bufferedImage, Integer.parseInt(width), Integer.parseInt(height), true);
                        whenReady = executor.submit(resizer);
                        try {
                            bufferedImage = whenReady.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                    }

                    if (color != null && color.equals("gray")){
                        ToGray toGray = new ToGray(bufferedImage);
                        whenReady = executor.submit(toGray);
                        try {
                            bufferedImage = whenReady.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                    response.setStatus(200);
                    async.complete();
                    ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
                }
            }

            @Override
            public void onError(Throwable t) {
                getServletContext().log("Async Error", t);
                async.complete();
            }
        });


    }

    public static void main(String[] args) throws Exception {

        QueuedThreadPool threadPool = new QueuedThreadPool(30,8);
        Server server = new Server(threadPool);
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.setConnectors(new Connector[]{serverConnector});

        //Server server = new Server(8080);
        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(ServerTest.class, "/");
        server.start();
        server.join();
    }

}