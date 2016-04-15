package com.cisco;

import java.io.IOException;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
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

public class RestServer extends AbstractVerticle {

	public void start(Future<Void> startFuture) {

		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.get("/user/:id").handler(new UserLoader());

		

		router.route().handler(StaticHandler.create()::handle);

		router.post("/user").handler(rc -> {
			System.out.println("POSSSSSS");
			String jsonStr = rc.getBodyAsString();

			ObjectMapper mapper = new ObjectMapper();

			UserDTO dto = null;
			try {
				dto = mapper.readValue(jsonStr, UserDTO.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			User u = dto.toModel();
			vertx.executeBlocking((future) -> {
				System.out.println("INSIDE ExecuteBlocking");
				Datastore dataStore = ServicesFactory.getMongoDB();
				dataStore.save(u);
				future.complete();
			}, res -> {
				if (res.succeeded()) {
					System.out.println("INSIDE succeeded");

					HttpServerResponse response = rc.response();
					response.setStatusCode(204).end("Data saved");
				} else {
					HttpServerResponse response = rc.response();
					response.setStatusCode(500).end("Data Not saved");
				}

			});
		});

		server.requestHandler(router::accept).listen(8080);
		System.out.println("RestServer started!");
		startFuture.complete();

	}

	@Override
	public void stop(Future stopFuture) throws Exception {
		System.out.println("RestServer stopped!");
	}

	public static void main(String[] args) {
		VertxOptions options = new VertxOptions().setWorkerPoolSize(10);
		Vertx vertx = Vertx.vertx(options);
		vertx.deployVerticle("com.cisco.RestServer", stringAsyncResult -> {
			System.out.println("RestServer deployment complete !!!");
			// vertx.deployVerticle("SecondVerticle");

		});
	}
}

class UserLoader implements Handler<RoutingContext> {

	public void handle(RoutingContext rc) {
		System.out.println("GETT333");
		HttpServerResponse response = rc.response();
		String id = rc.request().getParam("id");
		System.out.println("Received GET for ID " + id);

		response.putHeader("content-type", "application/json");
		Datastore dataStore = ServicesFactory.getMongoDB();

		rc.vertx().<List<User>>executeBlocking((future) -> {
			ObjectId oid = null;
			try {
				oid = new ObjectId(id);
			} catch (Exception e) {// Ignore format errors
			}
			List<User> users = dataStore.createQuery(User.class).field("id").equal(oid).asList();
			future.complete(users);
		}, (res) -> {
			if (res.succeeded()) {
				if (res.result().size() != 0) {
					List<User> usr = (List<User>) res.result();
					UserDTO dto = new UserDTO().fillFromModel(usr.get(0));
					ObjectMapper mapper = new ObjectMapper();
					JsonNode node = mapper.valueToTree(dto);
					response.end(node.toString());
				}

			} else {
				response.setStatusCode(404).end("not found");
			}
		}

		);

	}

}
