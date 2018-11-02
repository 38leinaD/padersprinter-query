package de.dplatz.padersprinter.control;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dplatz.padersprinter.entity.Departure;
import de.dplatz.padersprinter.entity.Location;

public class DeparturesServices {
	private static final Logger logger = Logger.getLogger(TripService.class.getName());

	private static final String DEPARTURE_NODES_XPATH = "//tr[contains(@class, 'std3_departure-line')]";

	HttpClient httpClient = new HttpClient();

	public CompletableFuture<List<Departure>> query(Location location) {
		final HtmlCleaner htmlCleaner = new HtmlCleaner();
		final DomSerializer domSerializer = new DomSerializer(new CleanerProperties());
		final XPath xpath = XPathFactory.newInstance().newXPath();

		return CompletableFuture.supplyAsync(() -> httpClient.get(location))
				.thenApply(r -> r.readEntity(String.class))
				.thenApply(htmlCleaner::clean)
				.thenApply(tagNode -> {
					try {
						Document doc = domSerializer.createDOM(tagNode);
						NodeList nodeList = (NodeList) xpath.evaluate(DEPARTURE_NODES_XPATH, doc,
								XPathConstants.NODESET);

						List<Departure> deps = new LinkedList<>();
						for (int i = 0; i < nodeList.getLength(); i++) {
							deps.add(parseDepature(nodeList.item(i)));
						}

						return deps;
					} catch (Exception e) {
						logger.info(e);
						return null;
					}
				});
	}

	static class HttpClient {

		public Response get(Location location) {
			final Client client = ClientBuilder.newClient();
			final long startTime = System.currentTimeMillis();
			final Response response = this
					.toWebTarget(location, client)
					.request()
					.header("X-Requested-With", "XMLHttpRequest")
					.get();
			final long duration = System.currentTimeMillis() - startTime;
			logger.info("get: " + duration + "ms");
			return response;
		}

		public WebTarget toWebTarget(Location location, Client client) {
			final WebTarget webtarget = client.target(
					"https://www.padersprinter.de/fahrplan/XSLT_DM_REQUEST?std3_mapDMMacro=true&language=de&name_dm={stopId}&type_dm=stopID&itdLPxx_template=tooltip&useRealtime=1")
					.resolveTemplate("stopId", location.getId());

			logger.info("Query URL is: " + webtarget.getUri().toString());
			return webtarget;
		}
	}

	Departure parseDepature(Node node) {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final LocalTime time;
		final String line;
		final String to;
		try {
			// LocalTime.parse
			String timeStr = parseStringNode(node, "./td[contains(@class, 'std3_dmTooltipTime')]/text()", xpath);
			time = LocalTime.parse(timeStr);
			Node lineNode = getNode(node,
					"./td[contains(@class, 'std3_dmTooltipService')]//span[contains(@class, 'std3_mot-label')]",
					xpath);
			line = lineNode.getTextContent().trim().split(" ")[1];

			to = parseStringNode(node, "./td[3]/text()", xpath);
		} catch (Exception ex) {
			logger.log(Level.ERROR, null, ex);
			return null;
		}

		Departure d = new Departure(time, line, to);
		logger.debug("Parsed departure: " + d);
		return d;
	}

	static String parseStringNode(Node node, String expr, XPath xpath) throws XPathExpressionException {
		String val = (String) xpath.evaluate(expr, node, XPathConstants.STRING);
		val = StringEscapeUtils.unescapeHtml4(val);
		return val;
	}

	static int parseIntegerNode(Node node, String expr, XPath xpath) throws XPathExpressionException {
		return Integer.parseInt((String) xpath.evaluate(expr, node, XPathConstants.STRING));
	}

	static boolean isNodePresent(Node node, String expr, XPath xpath) throws XPathExpressionException {
		return xpath.evaluate(expr, node, XPathConstants.NODE) != null;
	}

	static Node getNode(Node node, String expr, XPath xpath) throws XPathExpressionException {
		return (Node) xpath.evaluate(expr, node, XPathConstants.NODE);
	}
}
