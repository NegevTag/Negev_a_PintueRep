package com.example;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
// This class is the initializer of "Event".
public class EventFactory implements Finals {
    // Finals
    final static int MAX_MASSAGE_SIZE = 100;
    final static int RAND_STR_OPT = 4;
    final static int MAX_METRIC_VAL = 1000;

    // statics
    private static int nextReportID;
    private static int nextMetricID;

    // for saving metadata in mongo
    //keys in the mongo metadata : nextReportID,nextMetricID
    private static MongoClient mongoClient;
    private static DB database;
    private static DBCollection metaDataCollection;

    /**
     * Creates new event with uniuque metricId and reportId. 
     * 
     * @return new generated Event
     */
    public static Event create() {
        int nReportId = nextReportID;
        int nMetricId = nextMetricID;
        Date nTimeStamp = new Date(System.currentTimeMillis());
        int nMetricValue = generateRandMetricVal();
        nextReportID++;
        nextMetricID++;
        //empty query get first
        DBObject query = new BasicDBObject();
        
        //create dbobject to update
        BasicDBObject updateNextReportID = new BasicDBObject();
        BasicDBObject updateNextMetricID = new BasicDBObject();
        
        updateNextReportID.append("$set", new BasicDBObject(NEXT_REPORT_ID,nextReportID));
        updateNextMetricID.append("$set",new BasicDBObject(NEXT_METRIC_ID, nextMetricID));

        metaDataCollection.update(query, updateNextReportID);
        metaDataCollection.update(query, updateNextMetricID);
        return new Event(nReportId, nTimeStamp, nMetricId, nMetricValue, generateRandomSUString());
    }

    public static void initialize() {
       
        try {
             // mongo stuff
            mongoClient = new MongoClient(new MongoClientURI(Finals.MONGO_URL));
            // create database.
            database = mongoClient.getDB(Finals.MONGO_DB_NAME);
            // create collection
            metaDataCollection = database.getCollection(Finals.MONGO_META_DATA_COLLECTION);
            DBObject query = new BasicDBObject();
            //read from mongo 
            DBCursor cursor = metaDataCollection.find(query);
            DBObject metadata = cursor.one();
            //if no messege has been generated start id from zero
            if (metadata == null) {
                nextReportID = 0;
                nextMetricID = 0;
                
                BasicDBObject newMetadata = new BasicDBObject(NEXT_REPORT_ID,0).append(NEXT_REPORT_ID, 0);
                metaDataCollection.insert(newMetadata);
            }
            //if some messages have been generated start id by the last id
            else {
                nextReportID = (Integer)(metadata.get("nextReportID"));
                nextMetricID = (Integer)(metadata.get("nextMetricID"));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make Json String that represent the object
     */
    public static String toJson(Event event) {
        Gson g = new GsonBuilder().setDateFormat("MM dd, yyyy HH:mm:ss").create();
        return g.toJson(event);
    }

    /**
     * create event from object
     * 
     * @param json the json string
     * @return new event that was generated from json string
     */
    public static Event createFromJson(String json) {
        Gson g = new GsonBuilder().setDateFormat("MM dd, yyyy HH:mm:ss").create();
        return g.fromJson(json, Event.class);
    }

    /** generates random string */
    private static String generateRandomSUString() {
        Random rnd = new Random();
        int length = rnd.nextInt(MAX_MASSAGE_SIZE) + 1;
        String str = "";
        for (int i = 0; i < length; i++) {
            switch (rnd.nextInt(RAND_STR_OPT)) {
            case 0:
                str += "-";
                break;
            case 1:
                str += "a";
            case 2:
                str += "b";
            case 3:
                str += "c";
            default:
                break;
            }
        }
        return str;
    }

    // this method generates a random metric value(int).
    private static int generateRandMetricVal() {
        Random rnd = new Random();
        return rnd.nextInt(MAX_METRIC_VAL) + 1;

    }
}
