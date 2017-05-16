package com.pelikanit.wwm.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.pelikanit.wwm.WarmWaterManagement;
import com.pelikanit.wwm.admin.wwm.WarmWaterManagementService;
import com.pelikanit.wwm.utils.ConfigurationUtils;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class HttpsAdmin {
	
	private static final Logger logger = Logger.getLogger(
			HttpsAdmin.class.getCanonicalName());
	
	public static class IconHandler implements HttpHandler {
		
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			
			exchange.sendResponseHeaders(200, 0);
			final byte[] buffer = new byte[1024];
			int read;
			
			try (final InputStream in = getClass().getClassLoader().getResourceAsStream(
					"com/pelikanit/wwm/apple-touch-icon.png");
					final OutputStream out = exchange.getResponseBody()) {
				
				while ((read = in.read(buffer)) != -1) {
					
					out.write(buffer, 0, read);
					
				}
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
		}
		
	};
	
	private static final int REQUEST_BACKLOG = 5;
	
	private HttpsServer httpsServer;
	
	private HttpContextBuilder httpContextBuilder;
	
	public HttpsAdmin(final ConfigurationUtils config, final WarmWaterManagement wwm)
			throws Exception {
		
		final String host = config.getHttpsAdminHost();
		int port;
		try {
			port = config.getHttpsAdminPort();
		} catch (NoSuchElementException e) {
			port = 443;
		}
		
		final InetSocketAddress address;
		if (host == null) {
			address = new InetSocketAddress(port);
		} else {
			address = new InetSocketAddress(host, port);
		}
		
		httpsServer = HttpsServer.create(address, REQUEST_BACKLOG);
		
        char[] passphrase = config.getHttpsAdminKeystorePassword().toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(config.getHttpsAdminKeystore()), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
             public void configure (final HttpsParameters params) {
                SSLContext c = getSSLContext();

                 // get the default parameters
                 SSLParameters sslparams = c.getDefaultSSLParameters();

                 sslparams.setNeedClientAuth(params.getNeedClientAuth());
                 sslparams.setWantClientAuth(params.getWantClientAuth());
                 sslparams.setCipherSuites(params.getCipherSuites());
                 sslparams.setProtocols(params.getProtocols());

                 params.setSSLParameters(sslparams);
             }
        });
	
		httpsServer.createContext(WarmWaterManagementHttpHandler.PATH,
				new WarmWaterManagementHttpHandler());
		httpsServer.createContext("/apple-touch-icon.png", new IconHandler());
		/*
		adminContext.setAuthenticator(new BasicAuthenticator("get") {
			
			@Override
	        public boolean checkCredentials(String user, String pwd) {
	            return user.equals("admin") && pwd.equals("password");
	        }
			
	    });
	    */
		
		httpContextBuilder = new HttpContextBuilder();
		httpContextBuilder.setPath("/rest");

		final ResteasyDeployment deployment = httpContextBuilder.getDeployment();

		deployment.getActualResourceClasses().add(WarmWaterManagementService.class);
		
		httpContextBuilder.bind(httpsServer);

		final Dispatcher dispatcher = deployment.getDispatcher();
		dispatcher.getDefaultContextObjects().put(WarmWaterManagement.class, wwm);

	}
	
	public void start() {
		
		httpsServer.start();
		
		logger.info("Init of HttpServer complete");
		
	}
	
	public void stop() {
		
		httpContextBuilder.cleanup();
		
		httpsServer.stop(5);
		logger.info("Shutdown of HttpServer done...");
		
	}
	
}
