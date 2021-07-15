package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:16:13 PM
 */
public class HamiltonPathMovingEvent extends MovingEventBase {
    private static HamiltonPathMovingEvent instance;
    static {
        instance = new HamiltonPathMovingEvent();
    }

    private HamiltonPathMovingEvent() {

    }

    public static HamiltonPathMovingEvent getInstance() {
        return instance;
    }
    
    public void updateEventInfo() {

    }

    public Vector getCurrentEventInfo(int secOfDay) {
        return null;  
    }
}
