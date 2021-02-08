package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import javax.json.*;
import org.neo4j.graphdb.*;
import com.ProjectOne.api.Neo4jWithCache;

public class LinkedinObject 
{

	public Node commitToNeo4j(JsonObject json, Node rootNode) {

		if(!json.containsKey("formattedName") || !json.containsKey("id") ) {
			System.out.printf("No Name or id\n");
			return null;
		}

		Node personNode = Neo4jWithCache.lookupOrCreate( "people-id", "linkedin-id", json.get("id").toString().replace("\"", "") ); 

		String name="";
		JsonValue nameJson = json.get("formattedName");	
		if( nameJson != null) {
		name = json.get("formattedName").toString().replace("\"", "");	
		System.out.printf("NodeName1 : %s \n", name);
		}

	        List<String>    index = new ArrayList<String>();
                Map<String, String>     properties = new HashMap<String, String>();
                index.add("people");
		if( rootNode == null ) {
                	index.add("root");
		}
                properties.put("name", name);
                Neo4jWithCache.updatePropertyAndLabel(personNode, index, properties);

		
		if( rootNode != null) {

			Map<String, String>	relProps = new HashMap<String, String>();
			relProps.put("linkedin", "true");
			Neo4jWithCache.lookupOrCreateRelationship(rootNode, personNode, "knows", relProps); 

		}
/*
		if( json.containsKey("industry")) {
			Node professionNode = Neo4jWithCache.lookupOrCreate( "profession", "name", json.get("industry").toString().replace("\"", ""));

			Neo4jWithCache.createRelationship(personNode, professionNode, "practices", null); 
	


		}

		
		if( json.containsKey("location")) {

			JsonObject country= ((JsonObject) ((JsonObject)json.get("location")).get("country"));
			if( country.containsKey("code")) {
			String countryCode = country.get("code").toString().replace("\"", "");
			Node nationNode = Neo4jWithCache.lookupOrCreate("nation", "name", countryCode);;

			Neo4jWithCache.createRelationship(personNode, nationNode, "location", null); 

			}
		}
		
		if( json.containsKey("educations") && ((JsonObject)json.get("educations")).containsKey("values") ) {

			for( JsonValue education : ((JsonArray) ((JsonObject)json.get("educations")).get("values"))) {

			if( ((JsonObject)education).containsKey("schoolName")) {
			Node schoolNode = Neo4jWithCache.lookupOrCreate("school", "name", ((JsonObject)education).get("schoolName").toString().replace("\"", "") );
			
			Map<String, String> properties = new HashMap<String, String>();

			if( ((JsonObject)education).containsKey("startDate")) {	
				properties.put("startDate", ((JsonObject) ((JsonObject)education).get("startDate")).get("year").toString().replace("\"", ""));
			}
			if(((JsonObject) education).containsKey("endDate")) {	
				properties.put("endDate",((JsonObject) ((JsonObject)education).get("endDate")).get("year").toString().replace("\"", ""));
			}

			Neo4jWithCache.createRelationship(personNode, schoolNode, "studied", properties); 
			}	
			}

		}

		if( json.containsKey("positions") && ((JsonObject)json.get("positions")).containsKey("values") ) {

			for( JsonValue position : ((JsonArray) ((JsonObject)json.get("positions")).get("values"))) {

			if( ((JsonObject)position).containsKey("company") && ((JsonObject) ((JsonObject)position).get("company")).containsKey("name")) {
			Node companyNode = Neo4jWithCache.lookupOrCreate("company", "name",((JsonObject) ((JsonObject)position).get("company")).get("name").toString().replace("\"", "") );
			
			Map<String, String> properties = new HashMap<String, String>();

			if( ((JsonObject)position).containsKey("startDate")) {	
				properties.put("startDate", ((JsonObject)((JsonObject)position).get("startDate")).get("year").toString().replace("\"", ""));
			}
			if( ((JsonObject)position).containsKey("endDate")) {	
				properties.put("endDate", ((JsonObject)((JsonObject)position).get("endDate")).get("year").toString().replace("\"", ""));
			}

			Neo4jWithCache.createRelationship(personNode, companyNode, "worked", properties); 
			}	
			}



		}
		
	*/
		return personNode;
	}
}
