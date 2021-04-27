package vertx_token_auth;

import io.vertx.core.Vertx;
import vertx_token_auth.verticle.EmployeeVerticle;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Vertx vertx=Vertx.vertx();
        vertx.deployVerticle(new EmployeeVerticle());
    }
}
