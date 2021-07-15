package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:19:17 PM
 */
public class HourlyUniformDistributedMovingEvent extends MovingEventBase {
    private static HourlyUniformDistributedMovingEvent instance;
    static {
        instance = new HourlyUniformDistributedMovingEvent();
    }

    private HourlyUniformDistributedMovingEvent() {

    }

    public static HourlyUniformDistributedMovingEvent getInstance() {
        return instance;
    }
    
    public void updateEventInfo() {

    }

    public Vector getCurrentEventInfo(int secOfDay) {
        return null;  
    }
}
