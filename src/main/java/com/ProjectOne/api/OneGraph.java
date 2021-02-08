package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class OneGraph implements LinkGraph {

	private	String			name;
	private Map<String, String>	config;
	private JsonObject		graphObject;

	public OneGraph(String name, Map<String, String> config) {

		this.name = name;
		this.config = config;
		this.config.put("limit", "100" );
	}

	public void populate() {
		
		JsonArrayBuilder	arrayBuilder = Json.createArrayBuilder();
		Map<String, Object>	params = new HashMap<String,Object>();


		String query1 = 
		"match path = (n:people {name : {value} })-[rel:one*]->m return path";
		//"match path = (n:people {name : {value}})-[rel*..5]->m where type(rel[-1]) =\"one\" return path";

		
		System.out.println(query1);
		params.put("value", name);
		System.out.println( "value " + name);
		JsonObject result1 = Neo4jWithCache.runCypher(query1, params);
		
		AppJsonUtil.addJsonArrayToArrayBuilder(arrayBuilder,  (JsonArray)result1.get("paths")); 

		JsonObject result2 = Neo4jWithCache.getDirectPathsForNode("name", name, "people", "one", Direction.INCOMING, config); 
 
		
		AppJsonUtil.addJsonArrayToArrayBuilder(arrayBuilder, (JsonArray) result2.get("paths")); 

		JsonObjectBuilder	objectBuilder = Json.createObjectBuilder();

		objectBuilder.add("paths", arrayBuilder);	
		JsonObject result = objectBuilder.build(); 
	
		this.graphObject = result;
	}
/*
	public void populate() {
		
		JsonArrayBuilder	arrayBuilder = Json.createArrayBuilder();
		Map<String, Object>	params = new HashMap<String,Object>();
		String			skipStr = "";
		String			limitStr = " limit 100 ";

		if( config.containsKey("skip")) {
			skipStr = " skip " + config.get("skip");
		} 

		String query1 = 
		"match path = (n:people {name : {value} })-[rel:one*]->m return path";
		//"match path = (n:people {name : {value}})-[rel*..5]->m where type(rel[-1]) =\"one\" return path";

		
		System.out.println(query1);
		params.put("value", name);
		System.out.println( "value " + name);
		JsonObject result1 = Neo4jWithCache.runCypher(query1, params);
		
		AppJsonUtil.addJsonArrayToArrayBuilder(arrayBuilder,  (JsonArray)result1.get("paths")); 

		params.clear();
		String query2 = 
		"match path = m-[:one]->(n:people { name : {value} })  return path" + skipStr + limitStr ; 
		//"match path = (n:people {name : {value}})-[:knows]->m return path limit 100";
 
		System.out.println(query2);
		params.put("value", name);
		System.out.println( "value " + name);
		JsonObject result2 = Neo4jWithCache.runCypher(query2, params);
		
		AppJsonUtil.addJsonArrayToArrayBuilder(arrayBuilder, (JsonArray) result2.get("paths")); 

		JsonObjectBuilder	objectBuilder = Json.createObjectBuilder();

		objectBuilder.add("paths", arrayBuilder);	
		JsonObject result = objectBuilder.build(); 
	
		this.graphObject = result;
	}
*/
	public JsonObject graph() {

		return this.graphObject;
	}
		
}
