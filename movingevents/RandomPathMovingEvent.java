package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Oct 7, 2008
 * Time: 1:32:54 AM
 */
public class RandomPathMovingEvent extends MovingEventBase {
    private static RandomPathMovingEvent instance;
    static {
        instance = new RandomPathMovingEvent();
    }

    private RandomPathMovingEvent() {

    }

    public static RandomPathMovingEvent getInstance() {
        return instance;
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        return null;
    }

    public void updateEventInfo() {
        
    }
}
