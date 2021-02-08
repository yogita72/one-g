package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
 
public class MongoDB {

	private static MongoClient	mongo = null;	
	private static DB		db;
	public static void init(String dbName) {

                try {
                // intialize mongo
                mongo = new MongoClient( new ServerAddress("localhost" , 27017) );
                db = mongo.getDB(dbName);

                } catch(UnknownHostException e) {
                        e.printStackTrace();
                }

	}	

	public static void commit(String collectionName, String jsonData) {

	DBCollection	collection = db.getCollection(collectionName);
	DBObject 	dbObject = (DBObject) JSON.parse(jsonData);

	collection.insert(dbObject, WriteConcern.ACKNOWLEDGED);

	}

}
