package com.ProjectOne.api;

import java.io.*;
import java.util.*;
import com.ProjectOne.api.Neo4jWithCache;
import com.ProjectOne.api.MongoDB;
import com.ProjectOne.api.LinkedinObject;
import com.ProjectOne.api.AppJsonUtil;
import org.neo4j.graphdb.*;
import javax.json.*;

public class OneUploader implements Uploader{

	public void upload(String requestBody){

/*
		String mongoJson;

		mongoJson =  "{ commit:" + requestBody + "}";
		MongoDB.commit( "one", mongoJson);
*/
		Neo4jWithCache.createIndex( "one", "name");

		String[]		requestVars = requestBody.split(";", -1);
		
		String first = requestVars[0];
		String[] firstVars = first.split(":") ;
		System.out.printf("firstVars size :%d\n", firstVars.length);
		if( firstVars.length == 1 ) {
			
			Node 	rootNode = null;
			for( String name : requestVars ) {

				if( rootNode == null ) {
					System.out.println(name);
					if( !(name.equals(""))) {
						rootNode = commitToNeo4j(rootNode, name);
					}
				} else {
					System.out.println(name);
					if( !(name.equals(""))) {
						commitToNeo4j(rootNode, name);
					}
				}
			}

		} else {
		for( String pairs : requestVars ) {

			System.out.println( "pairs :" + pairs);
			if( ! (pairs.equals("")) ) {
				String[] nameVars = pairs.split(":", -1) ;
				commitToNeo4j(nameVars[0], nameVars[1]); 
			}
		}
		}
	}

	private void commitToNeo4j(String one, String person) {

		
		Node oneNode = Neo4jWithCache.lookupOrCreate( "people", "name", one); 

	       	List<String>    index = new ArrayList<String>();

               	Map<String, String>     properties = new HashMap<String, String>();
               	index.add("one");

               	Neo4jWithCache.updatePropertyAndLabel(oneNode, index, properties);
		Node personNode = Neo4jWithCache.lookupOrCreate( "people", "name", person); 
		
		Neo4jWithCache.lookupOrCreateRelationship(personNode, oneNode, "one", properties); 

	}

	private Node commitToNeo4j(Node rootNode, String person) {

		
               	Map<String, String>     properties = new HashMap<String, String>();

		if( rootNode == null ) {
			Node oneNode = Neo4jWithCache.lookupOrCreate( "people", "name", person); 

		       	List<String>    index = new ArrayList<String>();
	
       	        	index.add("one");

			System.out.println("creating node for :" + person);
       	        	Neo4jWithCache.updatePropertyAndLabel(oneNode, index, properties);
			rootNode = oneNode;
		} else {
			Node personNode = Neo4jWithCache.lookupOrCreate( "people", "name", person); 
		
			System.out.println("creating relationship for :" + person);
			Neo4jWithCache.lookupOrCreateRelationship(personNode, rootNode, "one", properties); 
		}
		return rootNode;
	}
}
