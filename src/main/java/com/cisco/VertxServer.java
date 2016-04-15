package com.cisco;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class VertxServer extends AbstractVerticle{

	 public void start(Future<Void> startFuture) {
	        
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.get("/services/new_users/:id").handler(this::handleUsers);
		router.get("/services/users/:id").handler(new UserLoader());
		
		server.requestHandler(router::accept).listen(8080);
		System.out.println("VertxServer started!");
		startFuture.complete();
	        
	 }
	 
	 private void handleUsers(RoutingContext routingContext){
		 
		 HttpServerResponse response = routingContext.response();
		 String message;
		 JsonObject json = new JsonObject();

		 json.put("test1", "value1");

		 message = json.toString();
		 System.out.println(message);
		 response.putHeader("content-type", "application/json").end(json.encodePrettily());
		 
	 }
	 
	 @Override
	public void stop(Future stopFuture) throws Exception {
	    System.out.println("VertxServer stopped!");
	}
	 public static void main(String[] args){
		 VertxOptions options = new VertxOptions().setWorkerPoolSize(10);
		 Vertx vertx = Vertx.vertx(options);
		 vertx.deployVerticle("com.cisco.VertxServer", stringAsyncResult -> {
	    		System.out.println("VertxServer deployment complete !!!");
	    	    //vertx.deployVerticle("SecondVerticle");
	    		
	    	});
	 }
}

/*class UserLoader implements Handler<RoutingContext> {
	
	public void handle(RoutingContext routingContext) {
		JsonObject json = new JsonObject();

		 json.put("test1", "value2");
		String id = routingContext.request().getParam("id");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json");
		response.end(json.encodePrettily());
	}
	
}*/