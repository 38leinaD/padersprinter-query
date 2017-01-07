/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import java.time.LocalTime;

/**
 *
 * @author daniel.platz
 */
public class Leg {

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String startLocation;
    private final String endLocation;
    private final Transport transport;

    public Leg(Transport transport, LocalTime startTime, String startLocation, LocalTime endTime, String endLocation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.transport = transport;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public Transport getTransport() {
        return transport;
    }

    @Override
    public String toString() {
        return "Leg{" + "startTime=" + startTime + ", endTime=" + endTime + ", startLocation=" + startLocation + ", endLocation=" + endLocation + ", transport=" + transport + '}';
    }
}
