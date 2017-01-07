package be.echostyle.moola;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import static sun.net.www.protocol.http.AuthCacheValue.Type.Server;

public class Main {

    public static void main(String[] args) {
        Server srv = new Server(0);
        ServletContextHandler sch = new ServletContextHandler();

    }

}
