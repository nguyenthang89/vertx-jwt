package vertx_token_auth.verticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import vertx_token_auth.model.Employee;


public class EmployeeVerticle extends AbstractVerticle
{
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
        router.get("/api/employees").handler(this::getAllEmployees);
        router.get("/api/employeessort").handler(this::getSortEmployees);
        router.get("/api/employees/:id").handler(this::getOneEmployee);
        router.route("/api/employees*").handler(BodyHandler.create());
        router.post("/api/employees").handler(this::insertNewEmployee);
        router.put("/api/employees").handler(this::updateOneEmpoyee);
        router.delete("/api/employees/:id").handler(this::deleteOneEmployee);
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

    /**
     * HĂ m nĂ y dĂ¹ng Ä‘á»ƒ láº¥y danh sĂ¡ch dá»¯ liá»‡u Employee mĂ  cĂ³ sáº¯p xáº¿p
     * http://localhost:8080/api/employeessort?sort=desc ->giáº£m dáº§n
     * http://localhost:8080/api/employeessort?sort=asc ->tÄƒng dáº§n
     * @param routingContext
     */
    private void getSortEmployees(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        //parameter láº¥y tá»« ngÆ°á»�i dĂ¹ng
        String sort = routingContext.request().getParam("sort");
        if (sort == null)
        {
            //náº¿u khĂ´ng cĂ³ thĂ¬ cho lá»—i luĂ´n API
            routingContext.response().setStatusCode(400).end();
        }
        else
        {
            //dĂ¹ng ArrayList Ä‘á»ƒ lÆ°u trá»¯ cĂ¡c Key cá»§a dá»¯ liá»‡u
            ArrayList<Integer> sortedKeys =
                    new ArrayList<Integer>(employees.keySet());
            //máº·c Ä‘á»‹nh sáº¯p xáº¿p tÄƒng dáº§n cĂ¡c Key
            Collections.sort(sortedKeys);
            //náº¿u sort lĂ  desc (giáº£m dáº§n)
            if(sort.equalsIgnoreCase("desc"))
            {
                //thĂ¬ Ä‘áº£o ngÆ°á»£c láº¡i danh sĂ¡ch Ä‘ang tÄƒng dáº§n -> nĂ³ tá»± thĂ nh giáº£m dáº§n
                Collections.reverse(sortedKeys);
            }
            //khai bĂ¡o danh sĂ¡ch Employee lĂ  ArrayList
            ArrayList<Employee>sortEmployees=new ArrayList<>();
            //vĂ²ng láº·p theo Key
            for (int key : sortedKeys)
            {
                //má»—i láº§n láº¥y employees.get(key) lĂ  Ä‘Ă£ láº¥y tÄƒng dáº§n hoáº·c giáº£m dáº§n (vĂ¬ key Ä‘Ă£ sáº¯p xáº¿p)
                sortEmployees.add(employees.get(key));
            }
            //tráº£ vá»� danh sĂ¡ch Ä‘Ă£ sáº¯p xĂªp
            response.end(Json.encodePrettily(sortEmployees));
        }
    }
    //HĂ m tráº£ vá»� thĂ´ng tin chi tiáº¿t cá»§a 1 Employee khi biáº¿t Id cá»§a há»�
    private void getOneEmployee(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=utf-8");
        //láº¥y id nháº­p tá»« URL
        String sid = routingContext.request().getParam("id");
        if (sid == null) {//náº¿u khĂ´ng tá»“n táº¡i thĂ¬ bĂ¡o lá»—i
            routingContext.response().setStatusCode(400).end();
        }
        else
        {
            //Ä‘Æ°a id Ä‘Ă³ vá»� sá»‘ (vĂ¬ dá»¯ liá»‡u láº¥y tá»« URL lĂ  chuá»—i
            int id=Integer.parseInt(sid);
            //tráº£ vá»� Empoyee cĂ³ mĂ£ lĂ  id
            Employee empFound=employees.get(id);
            //xuáº¥t Json chi tiáº¿t Employee ra cho client
            response.end(Json.encodePrettily(empFound));
        }
    }
    //HĂ m nháº­n Json Object Employee Ä‘á»ƒ lÆ°u vĂ o Server
    private void insertNewEmployee(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF8");
        try {
            //routingContext.getBody() láº¥y dá»¯ liá»‡u tá»« client gá»­i lĂªn, nĂ³ cĂ³ Ä‘á»‹nh dáº¡ng Json
            //Json.decodeValue(routingContext.getBody(),Employee.class);->Ä‘Æ°a Json Ä‘Ă³ vá»� Employee
            Employee emp=Json.decodeValue(routingContext.getBody(),Employee.class);
            //Ä‘Æ°a vĂ o HashMap
            employees.put(emp.getId(),emp);
            //xuáº¥t káº¿t quáº£ lĂ  true xuá»‘ng cho client náº¿u lÆ°u thĂ nh cĂ´ng
            response.end("true");
        }
        catch (Exception ex)
        {
            response.end(ex.getMessage());//lÆ°u tháº¥t báº¡i (khĂ¡c true)
        }
    }

    /**
     * HĂ m cáº­p nháº­t dá»¯ liá»‡u
     * @param routingContext
     */
    private void updateOneEmpoyee(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF8");
        //routingContext.getBody() láº¥y dá»¯ liá»‡u lĂ  cáº¥u trĂºc Json tá»« client
        //Json.decodeValue(routingContext.getBody(),Employee.class); mĂ´ hĂ¬nh hĂ³a lĂ  Java model
        Employee emp=Json.decodeValue(routingContext.getBody(),Employee.class);
        //kiá»ƒm tra id tá»“n táº¡i khĂ´ng
        if(employees.containsKey(emp.getId()))
        {
            employees.put(emp.getId(), emp);//náº¿u tá»‘n táº¡i thĂ¬ chá»‰nh sá»­a
            response.end("true");//tráº£ vá»� true khi chá»‰nh sá»­a thĂ nh cĂ´ng
        }
        else
            response.end("false");//tráº£ vá»� false khi chá»‰nh sá»­a tháº¥t báº¡i
    }

    /**
     * HĂ m nĂ y dĂ¹ng Ä‘á»ƒ xĂ³a má»™t Employee khi biáº¿t Id
     * @param routingContext
     */
    private void deleteOneEmployee(RoutingContext routingContext) {
        HttpServerResponse response=routingContext.response();
        response.putHeader("content-type","application/json;charset=UTF-8");
        //láº¥y id tá»« client
        String sid=routingContext.request().getParam("id");
        //Ä‘Æ°a vá»� int
        int id=Integer.parseInt(sid);
        //kiá»ƒm tra id tá»“n táº¡i hay khĂ´ng
        if(employees.containsKey(id)) {
            employees.remove(id);//cĂ³ thĂ¬ xĂ³a
            response.end("true");//xĂ³a thĂ nh cĂ´ng tráº£ vá»� true
        }
        else
            response.end("false");//khĂ´ng tĂ¬m tháº¥y tráº£ vá»� false
    }
}
