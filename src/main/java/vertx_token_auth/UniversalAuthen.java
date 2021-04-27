package vertx_token_auth;

import io.vertx.core.Vertx;
import vertx_token_auth.verticle.EmployeeAuthenVerticle;

public class UniversalAuthen {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Vertx vertx=Vertx.vertx();
        vertx.deployVerticle(new EmployeeAuthenVerticle());
	}

}
