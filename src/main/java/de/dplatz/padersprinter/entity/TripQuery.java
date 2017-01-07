/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.apache.log4j.Logger;

/**
 *
 * @author daniel.platz
 */
public class TripQuery {

    private static final Logger logger = Logger.getLogger(TripQuery.class.getName());

    final LocalDateTime start;
    final Location fromLocation;
    final Location toLocation;

    public TripQuery(LocalDateTime start, Location fromLocation, Location toLocation) {
        this.start = start;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    // TODO: move to where it belongs. this is not part of the query domain.
    public WebTarget toWebTarget(Client client) {
        // https://www.padersprinter.de/fahrplaninfo/fahrplanauskunft/?name_origin=Paderborn, Arndtstraße&type_origin=any&nameInfo_origin=streetID:1500000167::5774032:-1:Arndtstraße:Paderborn:Arndtstraße::Arndtstraße: 33100:ANY:DIVA_STREET:977432:5249189:MRCV:nrw&type_origin=any&name_destination=Paderborn, Heinz-Nixdorf-Ring&type_destination=any&nameInfo_destination=23207086&type_destination=any&date=30.12.2016 15:34&itdTripDateTimeDepArr=dep&name_via=&type_via=any&nameInfo_via=invalid&type_via=any&dwellTimeMinutes=0&maxChanges=9&changeSpeed=normal&inclMOT_0=on&inclMOT_1=on&inclMOT_2=on&inclMOT_3=on&inclMOT_4=on&inclMOT_5=on&inclMOT_6=on&inclMOT_7=on&inclMOT_8=on&inclMOT_9=on&inclMOT_10=on&inclMOT_11=on&ptOptionsActive=1
        //o: https://www.padersprinter.de/fahrplaninfo/fahrplanauskunft/?name_origin=Paderborn%2C+Arndtstra%C3%9Fe&type_origin=any&nameInfo_origin=streetID%3A1500000167%3A%3A5774032%3A-1%3AArndtstra%C3%9Fe%3APaderborn%3AArndtstra%C3%9Fe%3A%3AArndtstra%C3%9Fe%3A+33100%3AANY%3ADIVA_STREET%3A977432%3A5249189%3AMRCV%3Anrw&type_origin=any&name_destination=Paderborn%2C+Heinz-Nixdorf-Ring&type_destination=any&nameInfo_destination=23207086&type_destination=any&date=30.12.2016+15%3A34&itdTripDateTimeDepArr=dep&name_via=&type_via=any&nameInfo_via=invalid&type_via=any&dwellTimeMinutes=0&maxChanges=9&changeSpeed=normal&inclMOT_0=on&inclMOT_1=on&inclMOT_2=on&inclMOT_3=on&inclMOT_4=on&inclMOT_5=on&inclMOT_6=on&inclMOT_7=on&inclMOT_8=on&inclMOT_9=on&inclMOT_10=on&inclMOT_11=on&ptOptionsActive=1
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        final WebTarget webtarget = client.target("https://www.padersprinter.de/fahrplaninfo/fahrplanauskunft/?"
                + "name_origin={origin}&"
                + "type_origin=any&"
                + "nameInfo_origin={origin_info}&"
                + "type_origin=any&"
                + "name_destination={destination}&"
                + "type_destination=any&"
                + "nameInfo_destination={destination_info}&"
                + "type_destination=any&"
                + "date={date}&"
                + "itdTripDateTimeDepArr=dep&"
                + "name_via=&"
                + "type_via=any&"
                + "nameInfo_via=invalid&"
                + "type_via=any&"
                + "dwellTimeMinutes=0&"
                + "maxChanges=9&"
                + "changeSpeed=normal&"
                + "inclMOT_0=on&inclMOT_1=on&inclMOT_2=on&inclMOT_3=on&inclMOT_4=on&inclMOT_5=on&inclMOT_6=on&inclMOT_7=on&inclMOT_8=on&inclMOT_9=on&inclMOT_10=on&inclMOT_11=on&ptOptionsActive=0")
                .resolveTemplate("origin", fromLocation.getName())
                .resolveTemplate("origin_info", fromLocation.getToken())
                .resolveTemplate("destination", toLocation.getName())
                .resolveTemplate("destination_info", toLocation.getToken())
                .resolveTemplate("date", formatter.format(start));

        logger.info("Query URL is: " + webtarget.getUri().toString());
        return webtarget;
    }

    public static class Builder {

        private Location fromLocation;
        private Location toLocation;
        private LocalDate startDate;
        private LocalTime startTime;

        private Builder() {

        }

        public static Builder go() {
            return new Builder();
        }

        public Builder today() {
            startDate = LocalDate.now();
            return this;
        }

        public Builder now() {
            startDate = LocalDate.now();
            startTime = LocalTime.now();
            return this;
        }

        public Builder on(LocalDate date) {
            startDate = date;
            return this;
        }

        public Builder at(LocalTime time) {
            startTime = time;
            return this;
        }

        public Builder from(Location from) {
            this.fromLocation = from;
            return this;
        }

        public Builder to(Location to) {
            this.toLocation = to;
            return this;
        }

        public TripQuery query() {
            return new TripQuery(LocalDateTime.of(startDate, startTime), fromLocation, toLocation);
        }
    }
}
