package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.MongoDB;
import com.ProjectOne.api.LinkedinObject;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class LinkedinUploader implements Uploader{

	public void upload(String requestBody){


		MongoDB.commit( "linkedin", requestBody);

		JsonObject json = (JsonObject) AppJsonUtil.fromString(requestBody);


		Neo4jWithCache.createIndex( "people", "name");
		Neo4jWithCache.createIndex( "people-id", "linkedin-id");
		JsonObject profileJson = (JsonObject) json.get("profile");

		LinkedinObject profile = new LinkedinObject();

		Node rootNode = profile.commitToNeo4j(profileJson,null);
		
		JsonObject connectionsJson = (JsonObject) json.get("connections");
             	JsonArray jsonArray = (JsonArray)connectionsJson.get("values");
                Iterator<JsonValue> iterator = jsonArray.iterator();

                while( iterator.hasNext()) {
                        LinkedinObject connection = new LinkedinObject();
                        connection.commitToNeo4j((JsonObject)(iterator.next()), rootNode);
                }
	}
}
