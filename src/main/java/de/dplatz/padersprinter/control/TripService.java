/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.control;

import de.dplatz.padersprinter.entity.Leg;
import de.dplatz.padersprinter.entity.TripQuery;
import de.dplatz.padersprinter.entity.Transport;
import de.dplatz.padersprinter.entity.Trip;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.parsers.ParserConfigurationException;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rx.Observable;

/**
 *
 * @author daniel.platz
 */
public class TripService {

    private static final Logger logger = Logger.getLogger(TripService.class.getName());

    private static final String TRIP_NODES_XPATH = "//div[contains(@class, 'efa-result')]/div[contains(@class, 'col-md-12')]/div[contains(@class, 'panel panel-default')]";
    
    HttpClient httpClient = new HttpClient();

    public Observable<Trip> query(TripQuery query) {
        final HtmlCleaner htmlCleaner = new HtmlCleaner();
        final DomSerializer domSerializer = new DomSerializer(new CleanerProperties());
        final XPath xpath = XPathFactory.newInstance().newXPath();


        return httpClient.get(query)
                .map(htmlCleaner::clean)
                .flatMap(tagNode -> {
                    try {
                        return Observable.just(domSerializer.createDOM(tagNode));
                    } catch (ParserConfigurationException pce) {
                        return Observable.error(pce);
                    }
                })
                .flatMap(doc -> {
                    try {
                        return Observable.just((NodeList) xpath.evaluate(TRIP_NODES_XPATH, doc, XPathConstants.NODESET));
                    } catch (XPathExpressionException xee) {
                        return Observable.error(xee);
                    }
                })
                .flatMap(nodeList -> {
                    List<Node> nodes = new LinkedList<>();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        nodes.add(nodeList.item(i));
                    }
                    logger.info("HTML contains " + nodes.size() + " result-panels.");
                    return Observable.from(nodes);
                })
                .flatMap(tripNode -> parseTrip(tripNode).map(Observable::just).orElseGet(Observable::empty));
    }

    static class HttpClient {

        public Observable<String> get(TripQuery query) {
            return Observable.fromCallable(() -> {
                final Client client = ClientBuilder.newClient();
                final long startTime = System.currentTimeMillis();
                final String result = query.toWebTarget(client).request().get(String.class);
                final long duration = System.currentTimeMillis() - startTime;
                logger.info("get: " + duration + "ms");
                return result;
            });
        }
    }

    Optional<Trip> parseTrip(Node node) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final LocalTime begin;
        final LocalTime end;
        final String duration;
        final int transferCount;
        try {
            begin = LocalTime.parse(parseStringNode(node, "./div[contains(@class, 'panel-heading')]/table[contains(@class, 'tripTable')]//tbody/tr/td[2]/text()", xpath));
            end = LocalTime.parse(parseStringNode(node, "./div[contains(@class, 'panel-heading')]/table[contains(@class, 'tripTable')]//tbody/tr/td[3]/text()", xpath));
            duration = parseStringNode(node, "./div[contains(@class, 'panel-heading')]/table[contains(@class, 'tripTable')]//tbody/tr/td[4]/text()", xpath);
            transferCount = parseIntegerNode(node, "./div[contains(@class, 'panel-heading')]/table[contains(@class, 'tripTable')]//tbody/tr/td[5]/text()", xpath);
        } catch (Exception ex) {
            logger.log(Level.ERROR, null, ex);
            return Optional.empty();
        }

        Optional<List<Leg>> legs = parseLegs(node);
        if (legs.isPresent()) {
            Trip t = new Trip(begin, end, duration, transferCount, legs.get());
            logger.debug("Parsed trip: " + t);
            return Optional.of(t);
        } else {
            return Optional.empty();
        }
    }

    Optional<List<Leg>> parseLegs(Node node
    ) {
        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            NodeList legNodes = (NodeList) xpath.evaluate(".//table[contains(@class, 'legTable')]",
                    node, XPathConstants.NODESET);

            final List<Leg> legs = new LinkedList<>();

            logger.debug("Number of legs indentified: " + legNodes.getLength());
            for (int i = 0; i < legNodes.getLength(); i++) {
                Optional<Leg> leg = parseLeg(legNodes.item(i), xpath);

                if (!leg.isPresent()) {
                    logger.info("At least one leg could not be parsed. Ignoring trip.");
                    return Optional.empty();
                }

                legs.add(leg.get());
            }
            return Optional.of(legs);
        } catch (Exception ex) {
            logger.log(Level.ERROR, null, ex);
            return Optional.empty();
        }
    }

    Optional<Leg> parseLeg(Node legNode, XPath xpath) throws XPathExpressionException {
        final Node startNode = getNode(legNode, "./tbody/tr[./td[text() = 'ab']]", xpath);
        final Node endNode = getNode(legNode, "./tbody/tr[./td[text() = 'an']]", xpath);

        LocalTime startTime = LocalTime.parse(parseStringNode(startNode, "./td[1]", xpath));
        String startLocation = parseStringNode(startNode, "./td[4]", xpath);

        LocalTime endTime = LocalTime.parse(parseStringNode(endNode, "./td[1]", xpath));
        String endLocation = parseStringNode(endNode, "./td[4]", xpath);

        if (isNodePresent(legNode, ".//i[contains(@class, 'icon-pedestrian')]", xpath)) {
            final String id = parseStringNode(legNode, "./tbody/tr[.//i[contains(@class, 'icon-pedestrian')]]/td[4]", xpath);
            Leg l = new Leg(Transport.walk(id), startTime, startLocation, endTime, endLocation);
            return Optional.of(l);
        } else if (isNodePresent(legNode, ".//i[contains(@class, 'fa-bus')]", xpath)) {
            final String id = parseStringNode(legNode, "./tbody/tr[.//i[contains(@class, 'fa-bus')]]/td[4]", xpath);
            Leg l = new Leg(Transport.bus(id), startTime, startLocation, endTime, endLocation);
            return Optional.of(l);
        } else {
            logger.debug("Unknown leg-type: " + legNode);
            return Optional.empty();
        }
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
