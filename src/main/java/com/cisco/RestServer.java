	package com.cisco;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
//import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;


public class RestServer extends AbstractVerticle{

	 public void start(Future<Void> startFuture) {
	        
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		router.route().handler(StaticHandler.create()::handle);
		router.post("/user").handler(rc -> {
			String jsonStr = rc.getBodyAsString();
			
			ObjectMapper mapper = new ObjectMapper();
			UserDetails dto;
			try {
					dto = mapper.readValue(jsonStr, UserDetails.class);
					JsonNode node = mapper.valueToTree(dto);
					HttpServerResponse response = rc.response();
					response.end(node.toString()+" response");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		});
		server.requestHandler(router::accept).listen(8080);
		System.out.println("RestServer started!");
		startFuture.complete();
	        
	 }
	 
	
	 
	 @Override
	public void stop(Future stopFuture) throws Exception {
	    System.out.println("RestServer stopped!");
	}
	 public static void main(String[] args){
		 VertxOptions options = new VertxOptions().setWorkerPoolSize(10);
		 Vertx vertx = Vertx.vertx(options);
		 vertx.deployVerticle("com.cisco.RestServer", stringAsyncResult -> {
	    		System.out.println("RestServer deployment complete !!!");
	    	    //vertx.deployVerticle("SecondVerticle");
	    		
	    	});
	 }
}

class UserDetails {
	String uName;
	String passWord;
	public String getuName(){
		return uName;
	}
	public String getpassWord(){
		return uName;
	}
}

