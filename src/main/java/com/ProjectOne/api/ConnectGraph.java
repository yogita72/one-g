package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class ConnectGraph implements LinkGraph{

	private	String			name;
	private Map<String, String>	config;
	private JsonObject		graphObject;

	public ConnectGraph(String name, Map<String, String> config) {

		this.name = name;
		this.config = config;
		this.config.put("limit", "100" );
	}

	public void populate() {


		JsonObject result = Neo4jWithCache.getDirectPathsForNode("name", name, "people", "knows", Direction.BOTH, this.config); 
		this.graphObject = result;
	}
/*
	public void populate() {
		
		Map<String, Object>	params = new HashMap<String,Object>();
		String			skipStr = "";
		String			limitStr = " limit 100 ";

		if( config.containsKey("skip") ){
			skipStr = " skip " + config.get("skip");
		} 
		String query = 
		"match path = (n:people { name : {value} })-[:knows]->m return path" +  skipStr + limitStr ;

		System.out.println(query);
		params.put("value", name);
		System.out.println( "value " + name);
		JsonObject result = Neo4jWithCache.runCypher(query, params);
		
		this.graphObject = result;
	}
*/
	public JsonObject graph() {

		return this.graphObject;
	}
		
}
