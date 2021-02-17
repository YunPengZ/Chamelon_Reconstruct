package Bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FlightBean {
//    private int YEAR;
//    private int MONTH;
//    private int DAY;
    private int id;
    private int DAY_OF_WEEK;
    private String AIRLINE;
    private int FLIGHT_NUMBER;
    private String TAIL_NUMBER;
    private String ORIGIN_AIRPORT;
    private String DESTINATION_AIRPORT;
    private int SCHEDULED_DEPARTURE;
    private int DEPARTURE_TIME;
    private int DEPARTURE_DELAY;
    private int TAXI_OUT;
    private int WHEELS_OFF;
    private int SCHEDULED_TIME;
    private int ELAPSED_TIME;
    private int AIR_TIME;
    private int DISTANCE;
    private int WHEELS_ON;
    private int TAXI_IN;
    private int SCHEDULED_ARRIVAL;
    private int ARRIVAL_TIME;
    private int ARRIVAL_DELAY;
//    private int DIVERTED;
//    private int CANCELLED;

//    private String CANCELLATION_REASON;
//    private int AIR_SYSTEM_DELAY;
//    private int SECURITY_DELAY;
//    private int AIRLINE_DELAY;
//    private int LATE_AIRCRAFT_DELAY;
//    private int WEATHER_DELAY;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FlightBean(int id, String args[]) {
        this.id = id;
        this.DAY_OF_WEEK = Integer.parseInt(args[0]);
        this.AIRLINE = args[1];
        this.FLIGHT_NUMBER = Integer.parseInt(args[2]);
        this.TAIL_NUMBER = args[3];
        this.ORIGIN_AIRPORT = args[4];
        this.DESTINATION_AIRPORT = args[5];
        this.SCHEDULED_DEPARTURE = Integer.parseInt(args[6]);
        this.DEPARTURE_TIME = Double.valueOf(args[7]).intValue();
        this.DEPARTURE_DELAY = Double.valueOf(args[8]).intValue();
        this.TAXI_OUT = Double.valueOf(args[9]).intValue();
        this.WHEELS_OFF = Double.valueOf(args[10]).intValue();
        this.SCHEDULED_TIME = Double.valueOf(args[11]).intValue();
        this.ELAPSED_TIME = Double.valueOf(args[12]).intValue();
        this.AIR_TIME = Double.valueOf(args[13]).intValue();
        this.DISTANCE = Double.valueOf(args[14]).intValue();
        this.WHEELS_ON = Double.valueOf(args[15]).intValue();
        this.TAXI_IN = Double.valueOf(args[16]).intValue();
        this.SCHEDULED_ARRIVAL = Double.valueOf(args[17]).intValue();
        this.ARRIVAL_TIME = Double.valueOf(args[18]).intValue();
        this.ARRIVAL_DELAY = Double.valueOf(args[19]).intValue();
    }


    public String get(String para){

        String value= null;
        try {
            if (para.equals("DAY_OF_WEEK")){
                return Integer.toString(this.getDAY_OF_WEEK());
            }
            if (para.equals("AIRLINE")){
                return this.getAIRLINE();
            }
            if (para.equals("FLIGHT_NUMBER")){
                return Integer.toString(this.getFLIGHT_NUMBER());
            }
            if (para.equals("TAIL_NUMBER")){
                return this.getTAIL_NUMBER();
            }
            if (para.equals("ORIGIN_AIRPORT")){
                return this.getORIGIN_AIRPORT();
            }
            if (para.equals("DESTINATION_AIRPORT")){
                return this.getDESTINATION_AIRPORT();
            }
            if (para.equals("SCHEDULED_DEPARTURE")){
                return Integer.toString(this.getSCHEDULED_DEPARTURE());
            }
            if (para.equals("DEPARTURE_TIME")){
                return Integer.toString(this.getDEPARTURE_TIME());
            }
            if (para.equals("DEPARTURE_DELAY")){
                return Integer.toString(this.getDEPARTURE_DELAY());
            }
            if (para.equals("TAXI_OUT")){
                return Integer.toString(this.getTAXI_OUT());
            }
            if (para.equals("WHEELS_OFF")){
                return Integer.toString(this.getWHEELS_OFF());
            }
            if (para.equals("SCHEDULED_TIME")){
                return Integer.toString(this.getSCHEDULED_TIME());
            }
            if (para.equals("ELAPSED_TIME")){
                return Integer.toString(this.getELAPSED_TIME());
            }
            if (para.equals("AIR_TIME")){
                return Integer.toString(this.getAIR_TIME());
            }
            if (para.equals("DISTANCE")){
                return Integer.toString(this.getDISTANCE());
            }
            if (para.equals("WHEELS_ON")){
                return Integer.toString(this.getWHEELS_ON());
            }
            if (para.equals("TAXI_IN")){
                return Integer.toString(this.getTAXI_IN());
            }
            if (para.equals("SCHEDULED_ARRIVAL")){
                return Integer.toString(this.getSCHEDULED_ARRIVAL());
            }
            if (para.equals("ARRIVAL_TIME")){
                return Integer.toString(this.getARRIVAL_TIME());
            }
            if (para.equals("ARRIVAL_DELAY")){
                return Integer.toString(this.getARRIVAL_DELAY());
            }
            System.out.println("Flight unknown param: " + para);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return value;
    }

    public Integer getInt(String para){

        Integer value= null;
        try {
            if (para.equals("DAY_OF_WEEK")){
                return this.getDAY_OF_WEEK();
            }
            if (para.equals("FLIGHT_NUMBER")){
                return this.getFLIGHT_NUMBER();
            }
            if (para.equals("SCHEDULED_DEPARTURE")){
                return this.getSCHEDULED_DEPARTURE();
            }
            if (para.equals("DEPARTURE_TIME")){
                return this.getDEPARTURE_TIME();
            }
            if (para.equals("DEPARTURE_DELAY")){
                return this.getDEPARTURE_DELAY();
            }
            if (para.equals("TAXI_OUT")){
                return this.getTAXI_OUT();
            }
            if (para.equals("WHEELS_OFF")){
                return this.getWHEELS_OFF();
            }
            if (para.equals("SCHEDULED_TIME")){
                return this.getSCHEDULED_TIME();
            }
            if (para.equals("ELAPSED_TIME")){
                return this.getELAPSED_TIME();
            }
            if (para.equals("AIR_TIME")){
                return this.getAIR_TIME();
            }
            if (para.equals("DISTANCE")){
                return this.getDISTANCE();
            }
            if (para.equals("WHEELS_ON")){
                return this.getWHEELS_ON();
            }
            if (para.equals("TAXI_IN")){
                return this.getTAXI_IN();
            }
            if (para.equals("SCHEDULED_ARRIVAL")){
                return this.getSCHEDULED_ARRIVAL();
            }
            if (para.equals("ARRIVAL_TIME")){
                return this.getARRIVAL_TIME();
            }
            if (para.equals("ARRIVAL_DELAY")){
                return this.getARRIVAL_DELAY();
            }
            System.out.println("Flight unknown param: " + para);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return value;
    }

    public int getDAY_OF_WEEK() {
        return DAY_OF_WEEK;
    }

    public void setDAY_OF_WEEK(int DAY_OF_WEEK) {
        this.DAY_OF_WEEK = DAY_OF_WEEK;
    }

    public String getAIRLINE() {
        return AIRLINE;
    }

    public void setAIRLINE(String AIRLINE) {
        this.AIRLINE = AIRLINE;
    }

    public int getFLIGHT_NUMBER() {
        return FLIGHT_NUMBER;
    }

    public void setFLIGHT_NUMBER(int FLIGHT_NUMBER) {
        this.FLIGHT_NUMBER = FLIGHT_NUMBER;
    }

    public String getTAIL_NUMBER() {
        return TAIL_NUMBER;
    }

    public void setTAIL_NUMBER(String TAIL_NUMBER) {
        this.TAIL_NUMBER = TAIL_NUMBER;
    }

    public String getORIGIN_AIRPORT() {
        return ORIGIN_AIRPORT;
    }

    public void setORIGIN_AIRPORT(String ORIGIN_AIRPORT) {
        this.ORIGIN_AIRPORT = ORIGIN_AIRPORT;
    }

    public String getDESTINATION_AIRPORT() {
        return DESTINATION_AIRPORT;
    }

    public void setDESTINATION_AIRPORT(String DESTINATION_AIRPORT) {
        this.DESTINATION_AIRPORT = DESTINATION_AIRPORT;
    }

    public int getSCHEDULED_DEPARTURE() {
        return SCHEDULED_DEPARTURE;
    }

    public void setSCHEDULED_DEPARTURE(int SCHEDULED_DEPARTURE) {
        this.SCHEDULED_DEPARTURE = SCHEDULED_DEPARTURE;
    }

    public int getDEPARTURE_TIME() {
        return DEPARTURE_TIME;
    }

    public void setDEPARTURE_TIME(int DEPARTURE_TIME) {
        this.DEPARTURE_TIME = DEPARTURE_TIME;
    }

    public int getDEPARTURE_DELAY() {
        return DEPARTURE_DELAY;
    }

    public void setDEPARTURE_DELAY(int DEPARTURE_DELAY) {
        this.DEPARTURE_DELAY = DEPARTURE_DELAY;
    }

    public int getTAXI_OUT() {
        return TAXI_OUT;
    }

    public void setTAXI_OUT(int TAXI_OUT) {
        this.TAXI_OUT = TAXI_OUT;
    }

    public int getWHEELS_OFF() {
        return WHEELS_OFF;
    }

    public void setWHEELS_OFF(int WHEELS_OFF) {
        this.WHEELS_OFF = WHEELS_OFF;
    }

    public int getSCHEDULED_TIME() {
        return SCHEDULED_TIME;
    }

    public void setSCHEDULED_TIME(int SCHEDULED_TIME) {
        this.SCHEDULED_TIME = SCHEDULED_TIME;
    }

    public int getELAPSED_TIME() {
        return ELAPSED_TIME;
    }

    public void setELAPSED_TIME(int ELAPSED_TIME) {
        this.ELAPSED_TIME = ELAPSED_TIME;
    }

    public int getAIR_TIME() {
        return AIR_TIME;
    }

    public void setAIR_TIME(int AIR_TIME) {
        this.AIR_TIME = AIR_TIME;
    }

    public int getDISTANCE() {
        return DISTANCE;
    }

    public void setDISTANCE(int DISTANCE) {
        this.DISTANCE = DISTANCE;
    }

    public int getWHEELS_ON() {
        return WHEELS_ON;
    }

    public void setWHEELS_ON(int WHEELS_ON) {
        this.WHEELS_ON = WHEELS_ON;
    }

    public int getTAXI_IN() {
        return TAXI_IN;
    }

    public void setTAXI_IN(int TAXI_IN) {
        this.TAXI_IN = TAXI_IN;
    }

    public int getSCHEDULED_ARRIVAL() {
        return SCHEDULED_ARRIVAL;
    }

    public void setSCHEDULED_ARRIVAL(int SCHEDULED_ARRIVAL) {
        this.SCHEDULED_ARRIVAL = SCHEDULED_ARRIVAL;
    }

    public int getARRIVAL_TIME() {
        return ARRIVAL_TIME;
    }

    public void setARRIVAL_TIME(int ARRIVAL_TIME) {
        this.ARRIVAL_TIME = ARRIVAL_TIME;
    }

    public int getARRIVAL_DELAY() {
        return ARRIVAL_DELAY;
    }

    public void setARRIVAL_DELAY(int ARRIVAL_DELAY) {
        this.ARRIVAL_DELAY = ARRIVAL_DELAY;
    }
}
