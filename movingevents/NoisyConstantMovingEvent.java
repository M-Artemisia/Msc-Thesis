package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;
import drcl.inet.sensorsim.dynamicpointcoverage.DynamicPointCoverageConstants;
import drcl.comp.ACATimer;
import drcl.comp.ACARuntime;

import java.util.Vector;
import java.util.Random;
//import java.util.TimerTask;
//import java.util.Timer;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:14:38 PM
 */
public class NoisyConstantMovingEvent extends MovingEventBase {
    private static NoisyConstantMovingEvent instance = null;

    private final Integer mutex = new Integer(1);
    private int eventNum;
    private int meanEventNum;
    private Vector events;
    private Vector meanEvents;

    private Random eventNumRnd = new Random();
    private Random xRnd = new Random();
    private Random yRnd = new Random();
    private Random startTimeRnd = new Random();
    private Random stopTimeRnd = new Random();

    //Check whether any other event is set for the hour of this event in its vecinity
    private boolean isEventOk(int eventIndex, EventInfo eventInfo) {
        for (int i = 0;i < eventIndex;i++) {
            long curStartSec = ((EventInfo)events.get(i)).getStartSecond();
            long belowSec = (curStartSec / 3600) * 3600;
            long startSecond = eventInfo.getStartSecond();
            if (startSecond >= belowSec && startSecond < belowSec + 3600) {
                double curX = ((EventInfo)events.get(i)).getX();
                double curY = ((EventInfo)events.get(i)).getY();
                double eventX = eventInfo.getX();
                double eventY = eventInfo.getY();
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

    private NoisyConstantMovingEvent(ACARuntime runtime) {
        setRuntime(runtime);
        Random rnd = new Random();
        eventNum = rnd.nextInt(DynamicPointCoverageConstants.MAX_EVENT_NUMBER);
        meanEventNum = eventNum;
        events = new Vector();
        meanEvents = new Vector();
        for (int i = 0;i < eventNum;i++) {
            EventInfo eventInfo = new EventInfo();
            EventInfo meanEventInfo = new EventInfo();
            eventInfo.setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
            meanEventInfo.setX(eventInfo.getX());
            eventInfo.setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
            meanEventInfo.setY(eventInfo.getY());
            eventInfo.setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                                 DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
            while (!isEventOk(i, eventInfo)) {
                eventInfo.setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
                eventInfo.setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
                eventInfo.setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                                     DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
            }
            meanEventInfo.setStartSecond(eventInfo.getStartSecond());
            eventInfo.setStopSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY));
            if (eventInfo.getStartSecond() + DynamicPointCoverageConstants.LEAST_EVENT_DURATION >
                eventInfo.getStopSecond()) {
                eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                        DynamicPointCoverageConstants.LEAST_EVENT_DURATION);
            } else if (eventInfo.getStopSecond() - eventInfo.getStartSecond() >
                       DynamicPointCoverageConstants.MAX_EVENT_DURATION) {
                eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                        DynamicPointCoverageConstants.MAX_EVENT_DURATION);
            }
            meanEventInfo.setStopSecond(eventInfo.getStopSecond());
            events.add(eventInfo);
            meanEvents.add(meanEventInfo);
        }
        taskTimer = setTimeout("updateEventInfo", DynamicPointCoverageConstants.UPDATE_EVENT_INFO_PERIOD);
    }

    protected void timeout(Object data_) {
        if (data_.equals("updateEventInfo")) {
            updateEventInfo();
            taskTimer = setTimeout("updateEventInfo", DynamicPointCoverageConstants.UPDATE_EVENT_INFO_PERIOD);
        }
    }

    public void updateEventInfo() {
        //Noisy constant events should be changed by adding noisy values. We change the location and the start and(/or)
        // stop time of the occurrence of the events. Also, the number of events are changed randomly
        Random rnd = new Random();

        synchronized (mutex) {
            //1- Changing number of events according to a normal random variable
            eventNum = (int)(eventNumRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA + meanEventNum);

            //2nd, for each event, add noise to the locations, start and end time of the occurence
            events.clear();
            for (int i = 0;i < eventNum;i++) {
                if (i < meanEventNum) {
                    EventInfo eventInfo = new EventInfo();
                    eventInfo.setX(xRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                   ((EventInfo)meanEvents.get(i)).getX());
                    eventInfo.setY(yRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                   ((EventInfo)meanEvents.get(i)).getY());
                    eventInfo.setStartSecond((int)(startTimeRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                                   ((EventInfo)meanEvents.get(i)).getStartSecond()));
                    while (!isEventOk(i, eventInfo)) {
                        eventInfo.setX(xRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                       ((EventInfo)meanEvents.get(i)).getX());
                        eventInfo.setY(yRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                       ((EventInfo)meanEvents.get(i)).getY());
                        eventInfo.setStartSecond((int)(startTimeRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                                       ((EventInfo)meanEvents.get(i)).getStartSecond()));
                    }
                    eventInfo.setStopSecond((int)(stopTimeRnd.nextGaussian() * DynamicPointCoverageConstants.SIGMA +
                                                  ((EventInfo)meanEvents.get(i)).getStopSecond()));
                    if (eventInfo.getStartSecond() + DynamicPointCoverageConstants.LEAST_EVENT_DURATION >
                        eventInfo.getStopSecond()) {
                        eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                                DynamicPointCoverageConstants.LEAST_EVENT_DURATION);
                    } else if (eventInfo.getStopSecond() - eventInfo.getStartSecond() >
                               DynamicPointCoverageConstants.MAX_EVENT_DURATION) {
                        eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                                DynamicPointCoverageConstants.MAX_EVENT_DURATION);
                    }
                    events.add(eventInfo);
                } else {
                    EventInfo eventInfo = new EventInfo();
                    eventInfo.setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
                    eventInfo.setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
                    eventInfo.setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                             DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
                    while (!isEventOk(i, eventInfo)) {
                        eventInfo.setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
                        eventInfo.setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
                        eventInfo.setStartSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY -
                                                             DynamicPointCoverageConstants.LEAST_EVENT_DURATION));
                    }
                    eventInfo.setStopSecond(rnd.nextInt(DynamicPointCoverageConstants.MAX_SEC_PER_DAY));
                    if (eventInfo.getStartSecond() + DynamicPointCoverageConstants.LEAST_EVENT_DURATION >
                        eventInfo.getStopSecond()) {
                        eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                                DynamicPointCoverageConstants.LEAST_EVENT_DURATION);
                    } else if (eventInfo.getStopSecond() - eventInfo.getStartSecond() >
                               DynamicPointCoverageConstants.MAX_EVENT_DURATION) {
                        eventInfo.setStopSecond(eventInfo.getStartSecond() +
                                                DynamicPointCoverageConstants.MAX_EVENT_DURATION);
                    }
                    events.add(eventInfo);
                }
            }
            meanEvents.clear();
            meanEvents = (Vector)events.clone();
            meanEventNum = meanEvents.size();
        }
    }

    public static NoisyConstantMovingEvent getInstance(ACARuntime runtime) {
        if (instance == null) {
            instance = new NoisyConstantMovingEvent(runtime);
        }
        return instance;
    }

    public static NoisyConstantMovingEvent getInstance() {
        return instance;
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        Vector eventInfo = new Vector();
        synchronized (mutex) {
            for (int i = 0;i < eventNum;i++) {
                if (((EventInfo)events.get(i)).getStartSecond() < secOfDay &&
                    ((EventInfo)events.get(i)).getStopSecond() > secOfDay) {
                    eventInfo.add(events.get(i));
                }
            }
        }
        return eventInfo;
    }
}
