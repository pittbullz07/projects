/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */
package org.florinmatei.homework.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.florinmatei.homework.annotations.Context;
import org.florinmatei.homework.common.PropertyKeys;
import org.florinmatei.homework.common.Tuple;
import org.florinmatei.homework.file.DefaultFileManager;
import org.florinmatei.homework.file.FileManager;
import org.florinmatei.homework.file.ServerFile;
import org.florinmatei.homework.handler.DefaultFileHandler;
import org.florinmatei.homework.handler.FileHandler;
import org.florinmatei.homework.handler.Handler;
import org.florinmatei.homework.server.exceptions.ResponseException;
import org.florinmatei.homework.server.http.HttpConstants;
import org.florinmatei.homework.server.http.HttpSessionFactory;
import org.florinmatei.homework.utils.CloseableUtils;
import org.florinmatei.homework.utils.PropertyUtils;

/**
 * Base class of a server. Any server will need to extend this class.
 *
 * @author Florin Matei
 */
public abstract class BasicServer {

    private final Logger LOG;
    private ServerSocket socket;
    private Thread runningThread;
    private final String hostName;
    private String serverName;
    private int hostPort;
    private int keepAliveTimeout = 5000;
    private int minThreads;
    private int maxThreads;
    private Properties properties = new Properties();
    private ExecutorService executor;
    private Map<Class, ServiceProvider> services;
    private final Map<HttpMethod, Map<String, Tuple<Handler, Method>>> handlerMap;
    private FileHandler fileHandler;

    public BasicServer(Properties prop) {
        this.properties = prop;
        hostPort = PropertyUtils.getIntegerFromString(properties.getProperty(PropertyKeys.PROPERTY_SERVER_PORT, "8080"), 8080);
        if (hostPort < 0) {
            throw new IllegalArgumentException("Host Port cannot be negative!");
        }
        this.hostName = properties.getProperty(PropertyKeys.PROPERTY_HOST_NAME);
        serverName = properties.getProperty(PropertyKeys.PROPERTY_SERVER_NAME, "server");
        keepAliveTimeout = PropertyUtils.getIntegerFromString(properties.getProperty(PropertyKeys.PROPERTY_KEEP_ALIVE_TIME, "5000"), 5000);
        minThreads = PropertyUtils.getIntegerFromString(properties.getProperty(PropertyKeys.PROPERTY_THREADS_MIN, "2"), 2);
        maxThreads = PropertyUtils.getIntegerFromString(properties.getProperty(PropertyKeys.PROPERTY_THREADS_MAX, "50"), 50);
        final boolean prestartThreads = PropertyUtils.getBooleanFromString(properties.getProperty(PropertyKeys.PROPERTY_THREADS_PRESTART, "false"), false);
        services = new HashMap<Class, ServiceProvider>();

        final ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                minThreads,
                maxThreads,
                keepAliveTimeout,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ServerThreadFactory(serverName));

        if (prestartThreads) {
            tpe.prestartAllCoreThreads();
        }
        executor = tpe;

        handlerMap = new HashMap<>();
        fileHandler = new DefaultFileHandler(this);
        LOG = Logger.getLogger(serverName);
        registerServiceProvider(FileManager.class, new DefaultFileManager(new File(".")));
        registerServiceProvider(SessionFactory.class, new HttpSessionFactory(this));
    }

    /**
     * Starts the server. Once the server is started no more handlers or service
     * providers can be added.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        createHandlersList();
        socket = new ServerSocket();
        socket.bind((hostName != null) ? new InetSocketAddress(hostName, hostPort) : new InetSocketAddress(hostPort));
        LOG.log(Level.INFO, "Starting server {0}.\n Listening on port: {1}", new Object[]{serverName, Integer.toString(hostPort)});
        runningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (serverStarted()) {
                    try {
                        final Socket clientSocket = socket.accept();
                        //registerConnection(finalAccept);
                        handleRequest(clientSocket);
                    }
                    catch (IOException ioe) {
                        LOG.log(Level.SEVERE, "The connection was terminated", ioe);
                    }
                }
            }
        });
        runningThread.setDaemon(true);
        runningThread.setName(serverName + " Listener");
        runningThread.start();
    }

    /**
     * Stops the server if it is running, otherwise it won't do anything. This
     * will block until the currently executing tasks are drained.
     */
    public void stop() {
        if (serverStarted()) {
            executor.shutdown();
            try {
                //drain the remaining executing threads.
                executor.awaitTermination(keepAliveTimeout, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            CloseableUtils.safeClose(socket);
        }
    }

    /**
     * Lookups the current implementation for the required ServiceProvider
     * class.
     *
     * If no such provider is found null will be returned;
     *
     * @param providerClass
     * @return
     */
    public ServiceProvider lookupServiceProvider(final Class providerClass) {
        final ServiceProvider serviceProvider = services.get(providerClass);

        if (serviceProvider == null) {
            LOG.log(Level.WARNING, "No provider defined for " + providerClass.getName());
        }
        return serviceProvider;
    }

    public boolean serverStarted() {
        return socket != null && !socket.isClosed();
    }

    public Thread getThread() {
        return runningThread;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * THe only way to register custom ServiceProvider instances. This needs to
     * be done before the server is started using the {@link #start() } call.
     *
     * @param serviceClass
     * @param serviceProvider
     */
    protected final void registerServiceProvider(Class serviceClass, ServiceProvider serviceProvider) {
        if (serverStarted()) {
            LOG.log(Level.WARNING, "Trying to set a Service Provider after the server was started");
            throw new IllegalStateException("ServiceProvider implementations need to be registered before starting the server");
        }
        services.put(serviceClass, serviceProvider);
        LOG.log(Level.INFO, "Setting service provider {0} for {1} provider.", new Object[]{serviceProvider.getClass().getName(), serviceClass.getName()});
    }

    /**
     * Hook added to change the way the handling of requests is done.
     *
     * @param receiveSocket
     */
    protected void handleRequest(final Socket receiveSocket) {
        executor.execute(new RequestRunner(receiveSocket));
    }

    /**
     * THe only way to register contextual handlers. All the handlers need to be
     * registered before the server is started using {@link #start() }.
     * FileHandler instances cannot be registered this way.
     *
     * @param handler
     */
    protected void registerHandler(final Handler handler) {
        if (serverStarted()) {
            LOG.log(Level.WARNING, "Trying to register a Handler after the server was started");
            throw new IllegalStateException("Handlers can only pe registered before the server is started!");
        }
        final Method[] methods = handler.getClass().getMethods();

        for (final Method method : methods) {
            final Context context = method.getAnnotation(Context.class);
            if (context != null) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                final String url = context.url();
                final HttpMethod httpMmethod = context.method();
                boolean isMethodValid = parameterTypes != null && parameterTypes.length == 1 && parameterTypes[0].equals(Session.class);
                final boolean isContextValid = httpMmethod != null && url != null;
                if (isMethodValid && isContextValid) {

                    Map<String, Tuple<Handler, Method>> methodMap = handlerMap.get(httpMmethod);

                    if (methodMap == null) {
                        synchronized (handlerMap) {
                            methodMap = handlerMap.get(httpMmethod);

                            if (methodMap == null) {
                                methodMap = new HashMap<>();
                                handlerMap.put(httpMmethod, methodMap);
                            }
                        }
                    }

                    synchronized (methodMap) {
                        if (methodMap.containsKey(context.url())) {
                            LOG.log(Level.SEVERE, "Overwriting a previously set handler for the URL {0} with {1} from {2}", new Object[]{url, method.getName(), method.getDeclaringClass().getName()});
                        }
                        methodMap.put(url, new Tuple<Handler, Method>(handler, method));
                    }
                }
            }
        }
    }

    /**
     * The only way of registering the default FileHandler. This method needs to
     * be called before the server is started.
     *
     * @param handler
     */
    protected void setDefaultFileHandler(FileHandler handler) {

        if (serverStarted()) {
            throw new IllegalStateException("Cannot change FileHandler after the server has started");
        }
        if (fileHandler == null) {
            throw new IllegalArgumentException("File Handler instance cannot be null");
        }
        this.fileHandler = handler;
    }

    /**
     * Tries to find the handler for this request. If no Handler is registered
     * then it will return null.
     *
     * @param request
     * @return
     */
    private Tuple<Handler, Method> resolveMethod(final Request request) {

        Tuple<Handler, Method> returnValue = null;
        final HttpMethod httpMethod = request.getMethod();
        final String uri = request.getURI();

        final Map<String, Tuple<Handler, Method>> methodMap = handlerMap.get(httpMethod);
        if (methodMap != null) {
            returnValue = methodMap.get(uri);
        }
        return returnValue;
    }

    /**
     * Executes the invocation of the actual method that does the handling.
     *
     * @param session
     */
    private void runRequest(final Session session) {
        final Tuple<Handler, Method> tuple = resolveMethod(session.getRequest());
        try {
            if (tuple == null) {
                if (fileHandler != null) {
                    fileHandler.handle(session);
                } else {
                    generate404(session.getResponse());
                }
            } else {
                tuple.getParameter2().invoke(tuple.getParameter1(), session);
            }
        }
        catch (ResponseException ex) {
            LOG.log(Level.WARNING, "Could not process handle", ex);
            generateErrorResponse(ex, session.getResponse());
        }
        catch (Exception ex) {
            generateInternalErrorResponse(session.getResponse());
        }
    }

    /**
     * Generates a 500 Response.
     *
     * @param response
     */
    private void generateInternalErrorResponse(Response response) {
        response.setStatus(Status.INTERNAL_ERROR);
        response.setMimeType(MimeType.TEXT_HTML);
        response.setData("<html><title>Internal Server Error</title><body><h1>ERROR 500: Internal Server Error</h1></body></html>");
    }

    /**
     * Generates a 403 Response
     *
     * @param ex
     * @param response
     */
    private void generateErrorResponse(ResponseException ex, Response response) {
        response.setStatus(ex.getStatus());
        response.setMimeType(MimeType.TEXT_HTML);
        response.setData("<html><title>Error</title><body><h1>" + ex.getMessage() + "</h1></body></html>");
    }

    /**
     * Generates a 404 response
     *
     * @param response
     */
    private void generate404(Response response) {
        response.setStatus(Status.NOT_FOUND);
        response.setMimeType(MimeType.TEXT_HTML);
        response.setData("<html><title>404 Error</title><body><h1>Uhmmm...page not there :( (404 ERROR)</h1></body></html>");
    }

    private class RequestRunner implements Runnable {

        final Socket receiveSocket;

        public RequestRunner(final Socket receiveSocket) {
            this.receiveSocket = receiveSocket;
        }

        @Override
        public void run() {
            LOG.log(Level.INFO, "Thread {0} started", Thread.currentThread().getName());
            try {
                receiveSocket.setSoTimeout(keepAliveTimeout);
                final SessionFactory sessionFactory = (SessionFactory) BasicServer.this.lookupServiceProvider(SessionFactory.class);
                final Session session = sessionFactory.createSession(receiveSocket);

                while (!receiveSocket.isClosed()) {
                    session.next();
                    if (session.getRequest().getHeaders().get(HttpConstants.HeaderConstants.HEADER_CONNECTION) == null) {
                        receiveSocket.setSoTimeout(100);
                    } else {
                        receiveSocket.setSoTimeout(keepAliveTimeout);
                    }
                    LOG.log(Level.INFO, "Serving request: {0}", session.getRequest().toString());
                    runRequest(session);
                    session.sendResponse();
                    LOG.log(Level.INFO, "Response: {0}", session.getResponse().toString());
                }
            }
            catch (Exception e) {
                // When the socket is closed by the client, we throw our own SocketException
                // to break the  "keep alive" loop above.
                if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage()))) {
                    LOG.log(Level.SEVERE, "Exception", e);
                }
            }
            finally {
                CloseableUtils.safeClose(receiveSocket);
            }
            LOG.log(Level.INFO, "Thread {0} ended", Thread.currentThread().getName());
        }
    }

    private void createHandlersList() {
        FileManager fm = (FileManager) lookupServiceProvider(FileManager.class);
        try {
            ServerFile file = fm.createFile(null, "handlersList.html");

            StringBuilder builder = new StringBuilder("<html><body><h1>Available Handlers</h1><ul>");
            for (Entry<HttpMethod, Map<String, Tuple<Handler, Method>>> entry : handlerMap.entrySet()) {
                builder.append(entry.getKey().name()).append("\n<ul>");

                Map<String, Tuple<Handler, Method>> value = entry.getValue();

                for (Entry<String, Tuple<Handler, Method>> methodHandler : value.entrySet()) {
                    builder.append("<li>");
                    final boolean isGet = entry.getKey().equals(HttpMethod.GET);
                    if (isGet) {
                        builder.append("<a href=\"").append(methodHandler.getKey()).append("\">");
                    }
                    builder.append(methodHandler.getKey());
                    if (isGet) {
                        builder.append("</a>");
                    }
                    builder.append("</li>");
                }

                builder.append("</ul>");
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(file.write()));
            bufferedWriter.write(builder.toString(), 0, builder.length());
            bufferedWriter.flush();
            CloseableUtils.safeClose(bufferedWriter);
        }
        catch (ResponseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
