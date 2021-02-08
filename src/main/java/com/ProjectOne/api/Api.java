package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.MongoDB;
import com.ProjectOne.api.LinkedinObject;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class Api {

	private static boolean mongoInit = false;
	private static boolean neo4jInit = false;

	public static void init() {
		if( mongoInit == false) {
			MongoDB.init("people");
			mongoInit = true;
		}
		if( neo4jInit == false ){
			Neo4jWithCache.init();
			neo4jInit = true;
		}
	}
	public static String query( String query ) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		JsonObject  resultJson = Neo4jWithCache.runCypher(query, params);
		String result = AppJsonUtil.toString(resultJson);

		return result;
	}

	public static String graph(String names, boolean one, Map<String, String> config) {

		String[]		nameVars = names.split(",", -1);
		JsonArrayBuilder	arrayBuilder = Json.createArrayBuilder();		
		LinkGraph		linkgraph= null;

		for( String name : nameVars) {
	
			System.out.println("graph names" + name );
			if( one == true ) {
				linkgraph= new OneGraph(name, config);
			} else {
				linkgraph= new ConnectGraph(name, config);

			}
			linkgraph.populate();
			arrayBuilder.add(linkgraph.graph());
		}		

		JsonObjectBuilder	objectBuilder = Json.createObjectBuilder();
		
		objectBuilder.add("graphs", arrayBuilder);
		JsonObject graphsJson = objectBuilder.build();

		String graphsStr = AppJsonUtil.toString(graphsJson);
		return graphsStr;
		
	}



}
