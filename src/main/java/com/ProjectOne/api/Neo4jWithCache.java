package com.ProjectOne.api;

//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.exceptions.JedisConnectionException;
//import com.ProjectOne.app.GetJedis;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.*;
import org.neo4j.graphdb.schema.*;
import org.neo4j.index.lucene.*;
//import org.neo4j.index.*;
import org.neo4j.graphdb.index.*;
import org.neo4j.cypher.javacompat.*;
import org.neo4j.cypher.javacompat.ExecutionResult;
import javax.json.*;
import com.ProjectOne.api.AppJsonUtil;
/*
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.*;
import org.neo4j.graphdb.index.UniqueFactory.UniqueNodeFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueRelationshipFactory;
*/
public class Neo4jWithCache {

	private	static GraphDatabaseService graphDb;
	private static ExecutionEngine		cypherEngine;
//	private static Map<String, UniqueFactory<Node>> nodeFactories;
//	private static Map<String, UniqueFactory.UniqueNodeFactory> nodeFactories;
//	private static Map<String, UniqueFactory<Relationship>> relationFactories;

	private static enum RelTypes implements RelationshipType
	{
    		KNOWS
	}
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
    		// Registers a shutdown hook for the Neo4j instance so that it
    		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
    		// running application).
    		Runtime.getRuntime().addShutdownHook( new Thread()
    		{			
        		@Override
        		public void run()
        		{		
            			graphDb.shutdown();
        		}		
    		} );
	}

	public static void init() {
//		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( "/root/neo4j-community-2.1.0-M01/data/graph.db" );

		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder("/root/neo4j-community-2.1.0-M01/data/onegraph.db")
		.loadPropertiesFromFile("/root/neo4j-community-2.1.0-M01/conf/neo4j.properties")
		.newGraphDatabase() ;

		registerShutdownHook( graphDb );


		cypherEngine = new ExecutionEngine(graphDb);

		//nodeFactories = new HashMap<String, UniqueFactory.UniqueNodeFactory>();
//		nodeFactories = new HashMap<String, UniqueFactory<Node>>();
//		relationFactories = new HashMap<String, UniqueFactory<Relationship>>();

		}


	public static Relationship lookupOrCreateRelationship(Node node1, Node  node2, String type, Map<String, String> properties) {

		RelationshipType	relType = DynamicRelationshipType.withName(type);
		Relationship		rel = null;

		Transaction tx = graphDb.beginTx(); 

		try{

 			for(Relationship r : node1.getRelationships(relType, Direction.OUTGOING)) {
      				if (r.getOtherNode(node1).equals(node2)) { 
					rel = r;
					break;
				}
			}
   		 	if ( rel == null)  {
				rel = node1.createRelationshipTo( node2, relType);
			}
			if( properties != null ) {
			for(String prop : properties.keySet() ) {
				rel.setProperty( prop, properties.get(prop));

			}
			}
			tx.success();
		} finally {
			tx.finish();

		}
		return rel;
	}
	public static Relationship createRelationship(Node node1, Node  node2, String type, Map<String, String> properties) {

		RelationshipType	relType = DynamicRelationshipType.withName(type);
		Relationship		rel;

		Transaction tx = graphDb.beginTx(); 

		try{
			rel = node1.createRelationshipTo( node2, relType);

			if( properties != null ) {
			for(String prop : properties.keySet() ) {
				rel.setProperty( prop, properties.get(prop));

			}
			}
			tx.success();
		} finally {
			tx.finish();

		}
		return rel;
	}

	public static void createIndex(String index, String key) {



		Transaction tx = graphDb.beginTx(); 
	try{

			boolean		found = false ;
			Schema schema = graphDb.schema();
			Label	label	= DynamicLabel.label( index);
			Iterable<IndexDefinition> indexList = schema.getIndexes(label);

			for( IndexDefinition idxDef : indexList)
				found = true ;

			if( found == false ) {
			IndexDefinition	indexDefinition;
			indexDefinition = schema.indexFor(label)
            				.on( key)
            					.create();	
			System.out.printf("creating index on %s \n", index);
			} else {

			System.out.printf("index exists %s \n", index);

			}
    			tx.success();

		} finally {
			tx.finish();
		}
	}

	public static Node createNodeWithLabel( String index, String key, String value) {
		Node 	node;
		Transaction tx = graphDb.beginTx(); 
	try{
			Label label = DynamicLabel.label( index);
			node = graphDb.createNode(label);
		
			node.setProperty(key, value);

			

    			tx.success();

		} finally {
			tx.finish();
		}


		
		return node;	

	}

	public static Node createNodeWithLabel( List<String> index, Map<String, String> properties) {
		Node 	node;
		Transaction tx = graphDb.beginTx(); 
	try{
			node = graphDb.createNode();
		
			for(String idx: index) {	
				Label label = DynamicLabel.label( idx);
				node.addLabel(label);
			}
			for( String prop : properties.keySet()) {
				node.setProperty(prop, properties.get(prop));

			}

    			tx.success();

		} finally {
			tx.finish();
		}


		
		return node;	

	}


	public static ArrayList<Node> lookupNode( String index, String key, String value) {

		Label label = DynamicLabel.label( index );
		Transaction tx = graphDb.beginTx();
		ArrayList<Node> nodes = new ArrayList<Node>();
		try {
			ResourceIterator<Node> keys =
            			graphDb.findNodesByLabelAndProperty( label, key, value ).iterator();
		

        			while ( keys.hasNext() )
        			{
            				nodes.add( keys.next() );
        			}


			tx.success();
		} finally {

			tx.finish();
		} 


		return nodes;

	}

	public static void  updatePropertyAndLabel(Node node, List<String>index, Map<String,String> properties){
		Transaction tx = graphDb.beginTx(); 
	try{
		
			for(String idx: index) {	
				Label label = DynamicLabel.label( idx);
				node.addLabel(label);
			}
			for( String prop : properties.keySet()) {
				node.setProperty(prop, properties.get(prop));

			}

    			tx.success();

		} finally {
			tx.finish();
		}


		
	}



	
	public static Node lookup( String index, String key, String value){

		List<Node>	lookupNodes ;	
		Node 		node;
		lookupNodes = lookupNode(index, key,value);

		
		if( lookupNodes.size() == 0 ) {
			node = null ;
		} else {
			node = lookupNodes.get(0);
		}
		return node;

	}

	public static Node lookupOrCreate(String index, String key, String value) {

		Node 		node;

		node = lookup(index,key, value) ;

		if( node == null) {
			//createIndex(index, key);
			node = createNodeWithLabel(index, key, value);
		}


		return node;
	}
	
/*
	public static Node lookupOrCreate(Jedis jedis, String index, String key, String value) {

		Node 		node;
		String		cacheKey;
		String		nodeId;
		int		ex;

		cacheKey = "lookup" + ":" +  index + ":" + key + ":" + value;	
		ex = 600;

		nodeId = jedis.get(cacheKey) ;
		
		// get the node by nodeId 
		if ( nodeId != null ) {

			jedis.expire(cacheKey, ex);
		
			Transaction tx = graphDb.beginTx(); 
			try{
				node = graphDb.getNodeById(Long.parseLong(nodeId));

    				tx.success();

			} finally {
				tx.finish();
			}
		} else {
		node = lookup(index,key, value) ;

		if( node == null) {
			//createIndex(index, key);
			node = createNodeWithLabel(index, key, value);
		}

		// save to redis

		try {
			jedis.setex(cacheKey, ex, (Long.toString( node.getId())));	
			//System.out.printf("added to redis\n");
		} catch (JedisConnectionException e) {
			e.printStackTrace();
		}
		}

		return node;
	}
*/
/*
	public static String writeObjectToString(Object element) {

		String	resultStr = "{ ";
	try {
		

//     		byte b[] = element.getBytes(); 
 //    		ByteArrayInputStream bi = new ByteArrayInputStream(b);
     		DataInputStream si = new DataInputStream(element);

     		List<String>  list= si.readObject();

		for(String str : list) {
			resultStr = resultStr + str + ",";
		}
		resultStr = resultStr.substring(0, resultStr.length() -1 ) + "}";
		
 	} catch (Exception e) {
     		System.out.println(e);
 	}
	}
*/
/*
	public static String convertPath() {

  	while (rels.hasNext()){
            Path path = rels.next();

            Iterable<Relationship>  relationships = path.relationships();
            java.util.Iterator<Relationship> relIterator = relationships.iterator();
            while (relIterator.hasNext()){
                Relationship rel = relIterator.next();
                String aNode = (String) rel.getStartNode().getProperty("name");
                String zNode = (String) rel.getEndNode().getProperty("name");
                Long value = (Long) rel.getProperty("value");
                System.out.println(aNode +" is connected to "+zNode + " with value "+value);


            }


	}

*/
		public static JsonObject  runCypher(String query, Map<String,Object> params) {

			ExecutionResult	result;

			result = cypherEngine.execute(query, params);


		//	System.out.println( result.dumpToString() );
			JsonObject resultJson = processCypherResults(result);

			return resultJson;
}

		private static JsonObject	processCypherResults(ExecutionResult result) {

			ResourceIterator<Map<String,Object>> iterator = result.iterator();
			String		resultStr ="";
			boolean		nodesOrRels = false;
			JsonArrayBuilder	nodeArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder	linksArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder	pathArrayBuilder = Json.createArrayBuilder();
			Map<String,Object> mapElem = null;		

			while( iterator.hasNext() ) {
			
				mapElem = iterator.next();
				for(String key : mapElem.keySet()) {
				Object	nodeElem =   mapElem.get(key);
				String instanceType = getInstanceType(nodeElem);

				if( instanceType.equals("collection")) {

					processCollection((Collection) nodeElem,
								pathArrayBuilder);

				} else if( instanceType.equals("path")) {

					processPath( (org.neo4j.graphdb.Path)nodeElem,
							pathArrayBuilder);	

				} else if( instanceType.equals("relationship")){

					nodesOrRels = true;
					processRelationship( (Relationship) nodeElem,
								nodeArrayBuilder,
								linksArrayBuilder);

				} else if( instanceType.equals("node")) {

					nodesOrRels = true;
					processNode( (Node)nodeElem,
							nodeArrayBuilder);

				}
				
				}
			}
			iterator.close();

			if( nodesOrRels == true) {

				buildPath(pathArrayBuilder, 
					nodeArrayBuilder,
					linksArrayBuilder);
			}

			JsonObjectBuilder	resultObjectBuilder = Json.createObjectBuilder();
			resultObjectBuilder.add("paths", pathArrayBuilder); 
			JsonObject resultJson = resultObjectBuilder.build();	
			return resultJson;
		}


	public static void buildPath(JsonArrayBuilder pathArrayBuilder,
						JsonArrayBuilder nodeArrayBuilder,
						JsonArrayBuilder linksArrayBuilder) {

		JsonObjectBuilder	pathObjectBuilder = Json.createObjectBuilder();

		pathObjectBuilder.add("nodes", nodeArrayBuilder);
		pathObjectBuilder.add("links", linksArrayBuilder);

		pathArrayBuilder.add(pathObjectBuilder);

	}

	public static void processCollection(Collection	collection,
					JsonArrayBuilder pathArrayBuilder) {

		JsonArrayBuilder	nodeArrayBuilder = Json.createArrayBuilder();
		JsonArrayBuilder	linksArrayBuilder = Json.createArrayBuilder();
		boolean			nodesOrRels = false;
		
		System.out.println("in collection");
		for(Object entity : collection) {
			String instanceType =  getInstanceType(entity);  

			if( instanceType.equals("collection")) {

				processCollection((Collection) entity,
								pathArrayBuilder);

			} else if( instanceType.equals("path")) {

				processPath( (org.neo4j.graphdb.Path)entity,
						pathArrayBuilder);

			} else if( instanceType.equals("relationship")) {

				nodesOrRels = true;
				processRelationship( (Relationship)entity,
							nodeArrayBuilder,
							linksArrayBuilder);

			} else if( instanceType.equals("node")) {

				nodesOrRels = true;
				processNode( (Node)entity,
						nodeArrayBuilder);

			}
				
		}

		if( nodesOrRels == true) {

			buildPath(pathArrayBuilder, 
				nodeArrayBuilder,
				linksArrayBuilder);
		}
		

	}
	public static void processPath ( org.neo4j.graphdb.Path path, 
					JsonArrayBuilder pathArrayBuilder) {

		
		JsonArrayBuilder	nodeArrayBuilder = Json.createArrayBuilder();
		JsonArrayBuilder	linksArrayBuilder = Json.createArrayBuilder();

		System.out.println("in path");
            Iterable<Relationship>  relationships = path.relationships();
            java.util.Iterator<Relationship> relIterator = relationships.iterator();
            while (relIterator.hasNext()){
		
                Relationship rel = relIterator.next();
		processRelationship(rel, nodeArrayBuilder, linksArrayBuilder);
	    }

		buildPath(pathArrayBuilder, 
				nodeArrayBuilder,
				linksArrayBuilder);
	}

	public static void processRelationship ( Relationship  rel, 
					JsonArrayBuilder nodeArrayBuilder,
					JsonArrayBuilder linksArrayBuilder) {

		Transaction tx = graphDb.beginTx(); 

		try {
                Node source = rel.getStartNode();
		processNode(source, nodeArrayBuilder);
                Node target =  rel.getEndNode();
		processNode(target, nodeArrayBuilder);
                String value =  rel.getType().name();

		JsonObjectBuilder objectBuilder = Json.createObjectBuilder() ;
		objectBuilder.add("source", source.getId());
		objectBuilder.add("target", target.getId());
		objectBuilder.add("value", value);
		Iterable<String> iterator = rel.getPropertyKeys() ;

		String		property = "";
		for( String key : iterator ) {
			property = (String)rel.getProperty(key, null);	
			if( property != null )
				objectBuilder.add(key, property);
		}
			
		linksArrayBuilder.add(objectBuilder);		
		
		tx.success();
		}finally {
			tx.finish();

		}
	}

	public static void processNode ( Node  node, 
					JsonArrayBuilder nodeArrayBuilder) {

		
		Transaction tx = graphDb.beginTx(); 
		String		property = "";
		try {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder() ;
		objectBuilder.add("id", node.getId());

		Iterable<String> iterator = node.getPropertyKeys() ;

		for( String key : iterator ) {
			property = (String)node.getProperty(key, null);	
			if( property != null )
				objectBuilder.add(key, property);
		}

		// just adding irst label for now
		Iterable<Label> labels = node.getLabels();
		for( Label label : labels) {
			objectBuilder.add("label", label.name());
			break;
		}
		nodeArrayBuilder.add(objectBuilder);		

		tx.success();
		}finally {
			tx.finish();

		}
	}

	public static String getInstanceType( Object object) {

		String resultStr = "";
		if( object instanceof java.util.Collection) {

			resultStr = resultStr + "collection";
		} else  if( object instanceof org.neo4j.graphdb.Node){ 
			resultStr = resultStr + "node";
	
		} else if( object instanceof org.neo4j.graphdb.Path) {
			resultStr = resultStr + "path";
		} else if( object instanceof org.neo4j.graphdb.Relationship) {
			resultStr = resultStr + "relationship";
		} else if( object instanceof java.util.Iterator) {
		
			resultStr = resultStr + "iterator";
		} else { 
			resultStr = resultStr + object.getClass().getName() + "     ";
		}
		return resultStr;
	}


	public static JsonObject getDirectPathsForNode(String key, 
						String value, 
						String label, 
						String relationshipType, 
						Direction direction, 
						Map<String, String> config) {

		JsonArrayBuilder	pathArrayBuilder = Json.createArrayBuilder();
		RelationshipType	relType = DynamicRelationshipType.withName(relationshipType);
		Integer skip = 0;
		Integer limit = 9999999;

		if( config.containsKey("skip")) {	
			skip = Integer.parseInt(config.get("skip"));
		}
			
		if( config.containsKey("limit")) {	
			System.out.println("limit");
			limit = Integer.parseInt(config.get("limit"));
		}

		// get node by label and property
		
		ArrayList<Node> nodes = lookupNode( label, key, value) ;

		for( Node node : nodes ) {

			JsonArrayBuilder	nodeArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder	linksArrayBuilder = Json.createArrayBuilder();

			// get relationships by relationship type and direction
			
 			Iterator<Relationship> rels ;
			Transaction tx = graphDb.beginTx(); 

			try{
				System.out.println("transaction");
 				rels = node.getRelationships(relType, direction).iterator();

				System.out.printf("skip = %d; limit = %d", skip, limit);
				
			Integer i=0;
			//skip 
			for ( i=0 ; i < skip ; i++ ) {
				
				if( rels.hasNext() )
					rels.next();

			} 
			i = 0;
        		while ( rels.hasNext()  && i< limit) {

				// iterate based on config (limit and skip )

				processRelationship( rels.next(),
						nodeArrayBuilder,
						linksArrayBuilder);
				i++;
			}
		
				tx.success();
			} finally {
				tx.finish();

			}
			buildPath(pathArrayBuilder, 
				nodeArrayBuilder,
				linksArrayBuilder);

		}
		JsonObjectBuilder	resultObjectBuilder = Json.createObjectBuilder();
		resultObjectBuilder.add("paths", pathArrayBuilder); 
		JsonObject resultJson = resultObjectBuilder.build();	
		return resultJson;
	}			
}


