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
            //láº¥y dá»¯ liá»‡u lĂ  1 JsonObject á»Ÿ client truyá»�n lĂªn
            JsonObject data = context.getBodyAsJson();
            //Tui giáº£ sá»­ username: admin vĂ  password lĂ : 123
            /* cĂº phĂ¡p minh há»�a tá»« client gá»­i lĂªn:
            {
              "user": "admin",
              "pass": "123"
            }
            dÄ© nhiĂªn khi lĂ m MongoDB thĂ¬ mĂ¬nh sáº½ truy váº¥n account thá»±c táº¿, chá»‰ cáº§n Ä‘á»•i chá»— nĂ y thĂ´i, ráº¥t dá»…
            */
            if(!(data.getString("user").equals("admin") && data.getString("pass").equals("123")))
            {
                return;
            }
            //táº¡o token, háº¡n dĂ¹ng 60 giĂ¢y
            String token=jwtAuth.generateToken(new JsonObject(),
                    new JWTOptions().setExpiresInSeconds(60));
            //dĂ¹ng cookie lÆ°u xuá»‘ng phĂ­a client, trong vĂ²ng 60 giĂ¢y truy cáº¥p Ä‘Æ°á»£c cĂ¡c API thoáº£i mĂ¡i
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