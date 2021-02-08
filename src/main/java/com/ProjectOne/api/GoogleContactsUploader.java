package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.MongoDB;
import com.ProjectOne.api.GoogleObject;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class GoogleContactsUploader implements Uploader {

	public void upload(String requestBody){

//		MongoDB.commit( "google", requestBody);

		JsonObject json = (JsonObject) AppJsonUtil.fromString(requestBody);
		Node 	rootNode = null;

		Neo4jWithCache.createIndex( "people", "name");
		Neo4jWithCache.createIndex( "people-email", "email");

             	JsonArray jsonArray = (JsonArray)json.get("feeds");
		
		if( jsonArray != null ) {

                	Iterator<JsonValue> iterator = jsonArray.iterator();

                	while( iterator.hasNext()) {
				 JsonObject arrayElem = (JsonObject) AppJsonUtil.fromString((String)iterator.next().toString());
                       		 rootNode = uploadGoogleFeed((JsonObject)arrayElem.get("feed"), rootNode);
                	}
		} else {

                       	 rootNode = uploadGoogleFeed((JsonObject)json.get("feed"), rootNode);

		}	
	}


	private Node uploadGoogleFeed( JsonObject feedJson, Node rootNode) {

		if( rootNode == null) {

                        GoogleObject profile = new GoogleObject();
			rootNode = profile.commitToNeo4j( feedJson, null );
		}	
		
             	JsonArray jsonArray = (JsonArray)feedJson.get("entry");
		if( jsonArray != null ) {
                Iterator<JsonValue> iterator = jsonArray.iterator();

                while( iterator.hasNext()) {
                        GoogleObject connection = new GoogleObject();
                        connection.commitToNeo4j((JsonObject)(iterator.next()), rootNode);
                }
		} else {
		System.out.println("entry not found....");	
		}
		return rootNode;
	}
}
