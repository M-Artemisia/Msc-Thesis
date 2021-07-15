package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.DynamicPointCoverageConstants;
import drcl.inet.sensorsim.dynamicpointcoverage.TimeTracker;
import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;
import drcl.comp.ACARuntime;

import java.util.Vector;
import java.util.Random;

/**
 * Author: Esnaashari
 * Date: Oct 7, 2008
 * Time: 1:25:26 AM
 */
public class ConstantStraightPathMovingEvent extends MovingEventBase {
    
    class MovingObjectInfo {
        double startX;
        double startY;

        double curX;
        double curY;

        double endX;
        double endY;

        double velocity;

        double A;

        boolean reachToEndPoint;
    }

    final MovingObjectInfo[][] movingObjectInfos = new MovingObjectInfo[24][DynamicPointCoverageConstants.NUMBER_OF_MOVING_OBJECTS_PER_HOUR];

    private static ConstantStraightPathMovingEvent instance = null;

    private ConstantStraightPathMovingEvent(ACARuntime runtime) {
        Random rnd = new Random();
        for (int i = 0;i < 24;i++) {
            for (int j = 0;j < DynamicPointCoverageConstants.NUMBER_OF_MOVING_OBJECTS_PER_HOUR;j++) {
                movingObjectInfos[i][j] = new MovingObjectInfo();
                movingObjectInfos[i][j].startX = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X;
                movingObjectInfos[i][j].startY = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y;
                movingObjectInfos[i][j].endX = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X;
                movingObjectInfos[i][j].endY = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y;
                while (distanceIsLow(movingObjectInfos[i][j])) {
                    movingObjectInfos[i][j].endX = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X;
                    movingObjectInfos[i][j].endY = rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y;
                }
                movingObjectInfos[i][j].curX = movingObjectInfos[i][j].startX;
                movingObjectInfos[i][j].curY = movingObjectInfos[i][j].startY;
                movingObjectInfos[i][j].velocity = rnd.nextDouble() * DynamicPointCoverageConstants.MAX_VELOCITY;
                if (movingObjectInfos[i][j].endX != movingObjectInfos[i][j].startX) {
                    movingObjectInfos[i][j].A = (movingObjectInfos[i][j].endY - movingObjectInfos[i][j].startY) /
                                                (movingObjectInfos[i][j].endX - movingObjectInfos[i][j].startX);
                }
                movingObjectInfos[i][j].reachToEndPoint = false;
                setRuntime(runtime);
                taskTimer = setTimeout("updatePositionInfo", 1000); //we update the position every 1 seconds
            }
        }
    }

    private boolean distanceIsLow(MovingObjectInfo movingObjectInfo) {
        double d = dist(movingObjectInfo.startX, movingObjectInfo.startY, movingObjectInfo.endX, movingObjectInfo.endY);
        return d < DynamicPointCoverageConstants.LEAST_DISTANCE_FOR_MOVING_OBJECT *
                   DynamicPointCoverageConstants.LEAST_DISTANCE_FOR_MOVING_OBJECT;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static ConstantStraightPathMovingEvent getInstance(ACARuntime runtime) {
        if (instance == null) {
            instance = new ConstantStraightPathMovingEvent(runtime);
        }
        return instance;
    }

    protected void timeout(Object data_) {
        if (data_.equals("updatePositionInfo")) {
            updateEventInfo();
            taskTimer = setTimeout("updatePositionInfo", 1000); //we update the position every 1 seconds
        }
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        Vector eventsInfo = new Vector();
        int hour = TimeTracker.getInstance().getHour();
        for (int i = 0;i < DynamicPointCoverageConstants.NUMBER_OF_MOVING_OBJECTS_PER_HOUR;i++) {
            if (!movingObjectInfos[hour][i].reachToEndPoint) {
                EventInfo eventInfo = new EventInfo();
                eventInfo.setX(movingObjectInfos[hour][i].curX);
                eventInfo.setY(movingObjectInfos[hour][i].curY);
                eventsInfo.add(eventInfo);
            }
        }
        return eventsInfo;
    }

    public void updateEventInfo() {
        synchronized (movingObjectInfos) {
            int hour = TimeTracker.getInstance().getHour();
            int sec = TimeTracker.getInstance().getSec();
            if (sec <= 10) {
                for (int i = 0;i < DynamicPointCoverageConstants.NUMBER_OF_MOVING_OBJECTS_PER_HOUR;i++) {
                    movingObjectInfos[hour][i].curX = movingObjectInfos[hour][i].startX;
                    movingObjectInfos[hour][i].curY = movingObjectInfos[hour][i].startY;
                    movingObjectInfos[hour][i].reachToEndPoint = false;
                }
            }
            for (int i = 0;i < DynamicPointCoverageConstants.NUMBER_OF_MOVING_OBJECTS_PER_HOUR;i++) {
                if (!movingObjectInfos[hour][i].reachToEndPoint) {
                    if (movingObjectInfos[hour][i].startX == movingObjectInfos[hour][i].endX) {
                        if (movingObjectInfos[hour][i].endY > movingObjectInfos[hour][i].startY) {
                            movingObjectInfos[hour][i].curY += movingObjectInfos[hour][i].velocity;
                        } else {
                            movingObjectInfos[hour][i].curY -= movingObjectInfos[hour][i].velocity;
                        }
                    } else {
                        double tmp = Math.sqrt(1 + movingObjectInfos[hour][i].A * movingObjectInfos[hour][i].A);
                        double nextX1 = movingObjectInfos[hour][i].velocity / tmp + movingObjectInfos[hour][i].curX;
                        double nextX2 = -movingObjectInfos[hour][i].velocity / tmp + movingObjectInfos[hour][i].curX;

                        double nextY1 = movingObjectInfos[hour][i].A *
                                        movingObjectInfos[hour][i].velocity / tmp + movingObjectInfos[hour][i].curY;
                        double nextY2 = -movingObjectInfos[hour][i].A *
                                         movingObjectInfos[hour][i].velocity / tmp + movingObjectInfos[hour][i].curY;

                        double dist1 = dist(nextX1, nextY1, movingObjectInfos[hour][i].endX, movingObjectInfos[hour][i]. endY);
                        double dist2 = dist(nextX2, nextY2, movingObjectInfos[hour][i].endX, movingObjectInfos[hour][i]. endY);
                        if (dist1 < dist2) {
                            movingObjectInfos[hour][i].curX = nextX1;
                            movingObjectInfos[hour][i].curY = nextY1;
                            if (dist1 < .1) {
                                movingObjectInfos[hour][i].reachToEndPoint = true;
                            }
                        } else {
                            movingObjectInfos[hour][i].curX = nextX2;
                            movingObjectInfos[hour][i].curY = nextY2;
                            if (dist2 < .1) {
                                movingObjectInfos[hour][i].reachToEndPoint = true;
                            }
                        }
                    }
                }
            }
        }
    }

}
