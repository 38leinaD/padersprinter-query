/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author daniel.platz
 */
public class Trip {
    private final LocalTime begin;
    private final LocalTime end;
    private final String duration;
    private final int transferCount;
    private final List<Leg> legs;

    public Trip(LocalTime begin, LocalTime end, String duration, int transferCount, List<Leg> legs) {
        this.begin = begin;
        this.end = end;
        this.duration = duration;
        this.transferCount = transferCount;
        this.legs = legs;
    }

    @Override
    public String toString() {
        return "Trip{" + "begin=" + begin + ", end=" + end + ", duration=" + duration + ", transferCount=" + transferCount + ",\n  legs=" + legs + '}';
    }
    
    public LocalTime getBegin() {
        return begin;
    }

    public LocalTime getEnd() {
        return end;
    }

    public String getDuration() {
        return duration;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public List<Leg> getLegs() {
        return legs;
    }
}
