package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;
import drcl.inet.sensorsim.dynamicpointcoverage.DynamicPointCoverageConstants;

import java.util.Random;
import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:13:40 PM
 */
public class ConstantMovingEvent extends MovingEventBase {
    private static ConstantMovingEvent instance;


    private int eventNum;
    private EventInfo[] events;
    static {
        instance = new ConstantMovingEvent();
    }

    //Check whether any other event is set for the hour of this event in its vecinity
    private boolean isEventOk(int eventIndex) {
        for (int i = 0;i < eventIndex;i++) {
            long curStartSec = events[i].getStartSecond();
            long belowSec = (curStartSec / 3600) * 3600;
            long startSecond = events[eventIndex].getStartSecond();
            if (startSecond >= belowSec && startSecond < belowSec + 3600) {
                double curX = events[i].getX();
                double curY = events[i].getY();
                double eventX = events[eventIndex].getX();
                double eventY = events[eventIndex].getY();
                if ((curX - eventX) * (curX - eventX) +
                    (curY - eventY) * (curY - eventY) <=
                    (DynamicPointCoverageConstants.SENSE_DISTANCE * 2) *
                    (DynamicPointCoverageConstants.SENSE_DISTANCE * 2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private ConstantMovingEvent() {
        Random rnd = new Random();
        eventNum = rnd.nextInt(DynamicPointCoverageConstants.MAX_EVENT_NUMBER);
        events = new EventInfo[eventNum];
        for (int i = 0;i < eventNum;i++) {
            events[i] = new EventInfo();
            events[i].setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
            events[i].setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
            events[i].setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                                 DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
            while (!isEventOk(i)) {
                events[i].setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
                events[i].setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
                events[i].setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                                     DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
            }
            events[i].setStopSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY));
            if (events[i].getStartSecond() + DynamicPointCoverageConstants.LEAST_EVENT_DURATION >
                events[i].getStopSecond()) {
                events[i].setStopSecond(events[i].getStartSecond() +
                                        DynamicPointCoverageConstants.LEAST_EVENT_DURATION);
            } else if (events[i].getStopSecond() - events[i].getStartSecond() >
                       DynamicPointCoverageConstants.MAX_EVENT_DURATION) {
                events[i].setStopSecond(events[i].getStartSecond() +
                                        DynamicPointCoverageConstants.MAX_EVENT_DURATION);
            }
        }
    }

    public void updateEventInfo() {
        //Constant events are constant :D. No update is required then.
    }

    public static ConstantMovingEvent getInstance() {
        return instance;
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        Vector eventInfo = new Vector();
        for (int i = 0;i < eventNum;i++) {
            if (events[i].getStartSecond() < secOfDay &&
                events[i].getStopSecond() > secOfDay) {
                eventInfo.add(events[i]);
            }
        }
        return eventInfo;
    }
}
