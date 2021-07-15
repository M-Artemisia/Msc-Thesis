package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import java.util.TimerTask;

/**
 * Author: Esnaashari
 * Date: Aug 31, 2008
 * Time: 6:33:14 PM
 */
public class UpdateMovingEventTimerTask extends TimerTask {
    IMovingEvent movingEvent;

    public UpdateMovingEventTimerTask(IMovingEvent movingEvent) {
        super();
        this.movingEvent = movingEvent;
    }

    public void run() {
        movingEvent.updateEventInfo();
    }
}
