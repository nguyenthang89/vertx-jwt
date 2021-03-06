package vertx_token_auth.verticle;

import java.util.HashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import vertx_token_auth.model.Employee;

public class EmployeeAuthenVerticle  extends AbstractVerticle {
    private HashMap<Integer, Employee> employees=new HashMap<>();
    public  void createExampleData()
    {
        employees.put(1,new Employee(1,"Mr Obama","Obama@gmail.com"));
        employees.put(2,new Employee(2,"Mr Donald Trump","Trump@gmail.com"));
        employees.put(3,new Employee(3,"Mr Putin","Putin@gmail.com"));
    }
    private void getAllEmployees(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        response.end(Json.encodePrettily(employees.values()));
    }
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        createExampleData();
        Router router=Router.router(vertx);
        JWTAuth jwt=JWTAuth.create(vertx,
                new JsonObject()
                        .put("keyStore",
                                new JsonObject()
                                        .put("type","jceks")
                                        .put("path","keys\\keystore.jceks")
                                        .put("password","secret")));
        router.route("/api/*").handler(authHandler(jwt));
        router.get("/api/employees").handler(this::getAllEmployees);
        router.route("/api*").handler(BodyHandler.create());
        router.post("/api/login").handler(ctx->{login(ctx,jwt);});
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                startPromise.complete();
                            } else {
                                startPromise.fail(result.cause());
                            }
                        }
                );
    }
    public JWTAuthHandler authHandler(JWTAuth jwtAuth)
    {
        return JWTAuthHandler.create(jwtAuth,"/api/login");
    }
    public void login(RoutingContext context,JWTAuth jwtAuth)
    {
        try
        {
            //l??????y d?????? li???????u l???? 1 JsonObject ?????? client truy???????n l????n
            JsonObject data = context.getBodyAsJson();
            //Tui gi?????? s?????? username: admin v???? password l????: 123
            /* c???? ph????p minh h???????a t?????? client g??????i l????n:
            {
              "user": "admin",
              "pass": "123"
            }
            d???? nhi????n khi l????m MongoDB th???? m????nh s?????? truy v??????n account th??????c t??????, ch??????? c??????n ????????????i ch??????? n????y th????i, r??????t d???????
            */
            if(!(data.getString("user").equals("admin") && data.getString("pass").equals("123")))
            {
                return;
            }
            //t??????o token, h??????n d????ng 60 gi????y
            String token=jwtAuth.generateToken(new JsonObject(),
                    new JWTOptions().setExpiresInSeconds(60));
            //d????ng cookie l????u xu???????ng ph????a client, trong v????ng 60 gi????y truy c??????p ???????????????c c????c API tho??????i m????i
            Cookie cookie=Cookie.cookie("auth",token);
            cookie.setHttpOnly(true).setPath("/").encode();
            context.addCookie(cookie).response()
                    .putHeader("content-type","text/plain")
                    .putHeader("Authorization",token)
                    .end(token);
        }
        catch (Exception ex)
        {
            context.response().setStatusCode(401)
                    .putHeader("content-type","application/json")
                    .end(ex.getMessage());
        }
    }
}