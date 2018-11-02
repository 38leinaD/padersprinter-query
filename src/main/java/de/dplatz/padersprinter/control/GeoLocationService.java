package de.dplatz.padersprinter.control;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import de.dplatz.padersprinter.entity.GeoLocationQuery;
import de.dplatz.padersprinter.entity.Location;
import de.dplatz.padersprinter.entity.Location.Type;

public class GeoLocationService {

	private static final Logger logger = Logger.getLogger(LocationService.class.getName());

	static class HttpClient {

		public Response get(GeoLocationQuery query) {
			final Client client = ClientBuilder.newClient()
					.register(AjaxResponseFilter.class);
			final long startTime = System.currentTimeMillis();
			final Response response = query
					.toWebTarget(client)
					.request(MediaType.APPLICATION_JSON)
					.get();
			final long duration = System.currentTimeMillis() - startTime;
			logger.info("get: " + duration + "ms");
			return response;
		}
	}

	HttpClient httpClient = new HttpClient();

	CompletableFuture<List<Location>> search(GeoLocationQuery query) {
		return CompletableFuture.supplyAsync(() -> httpClient.get(query))
				.thenApply(this::mapHttpResponseToJson)
				.thenApply(this::mapToLocations);
	}

	private JsonStructure mapHttpResponseToJson(Response response) {
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			logger.info("Response-code: " + response.getStatus());
			return null;
		}
		response.bufferEntity();
		try {
			return response.readEntity(JsonArray.class);
		} catch (ClassCastException e) {
			logger.trace("Response is no array. Trying to parse as object.", e);
			return response.readEntity(JsonObject.class).getJsonObject("point");
		}
	}

	private Location mapPinToLocation(JsonObject pin) {
		String coords = pin.getString("coords");
		String desc = pin.getString("desc");
		String id = pin.getString("id");
		String type = pin.getString("type");

		return new Location(desc, Type.STOP, id, "?", 1, "A description");
	}

	private List<Location> mapToLocations(JsonStructure jsonObject) {
		if (jsonObject instanceof JsonObject) {
			JsonObject obj = JsonObject.class.cast(jsonObject);
			JsonArray pins = obj.getJsonArray("pins");

			return pins.stream()
					.map(p -> this.mapPinToLocation((JsonObject) p))
					.collect(toList());
		} else {
			logger.warn("Unexpected instance type " + jsonObject.getClass());
			return null;
		}
	}
}