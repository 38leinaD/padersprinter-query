/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter;

import de.dplatz.padersprinter.entity.Location;

/**
 *
 * @author daniel.platz
 */
public class TestData {
    public static class Locations {
        public static final Location Offices = new Location("Paderborn, Heinz-Nixdorf-Ring", Location.Type.Stop, "23207086", "23207086", 0, "Work");
        public static final Location City = new Location("Paderborn, Am Bogen", Location.Type.Stop, "23207008", "23207008", 0, "City");
    }
}
