package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _09_ReactomeImport {

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
	
	private static String timeConversion(long seconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    long minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    long hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;

	    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}
	
	static String DB_PATH = "/biograph.db";
	
	public static void main(String[] args) throws IOException {
		String homeDir = System.getProperty("user.home");
		File dbPath = new File(homeDir + DB_PATH);
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook( graphDb );
		
        int entryCounter = 0;
        int edgeCounter = 0;
        long startTime = System.currentTimeMillis();
        String line;
        String fileName = homeDir + "/biodb/reactome/pathways.txt";
        
        System.out.print("\nReading homo sapiens Reactome entries from " + fileName + " ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        HashMap<String, String> pathwayName = new HashMap<String, String>();
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	String id = datavalue[0];
        	String name = datavalue[1];
        	
        	pathwayName.put(id, name);
        }
        reader.close();
        
        fileName = homeDir + "/biodb/reactome/pathwayDisease.txt";
        System.out.println("\nReading pathways diseases from " + fileName);
        HashMap<String, String> pathwayDisease = new HashMap<String, String>();

        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine(); //skip header line
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	if (datavalue.length <= 1)
        		continue;
        	
        	String id = datavalue[0];
        	String disease = datavalue[1];
        	
        	pathwayDisease.put(id, disease);
        }
        reader.close();
        
		fileName = homeDir + "/biodb/reactome/pathwaySummation.txt";
        System.out.println("\nReading pathways summations from " + fileName);
        HashMap<String, String> pathwaySummation = new HashMap<String, String>();

        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine(); //skip header line
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	if (datavalue.length <= 1)
        		continue;
        	
        	String id = datavalue[0];
        	String summation = datavalue[1];
        	
        	pathwaySummation.put(id, summation);
        }
        reader.close();
        
		fileName = homeDir + "/biodb/reactome/pathwayLiteratureReference.txt";
        System.out.println("\nReading literature references from " + fileName);
        HashMap<String, ArrayList<String>> pathwayLiteratureReference = new HashMap<String, ArrayList<String>>();

        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine(); //skip header line
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	String id = datavalue[0];
        	String pubmedId = datavalue[1];
        	ArrayList<String> idList;
        	
        	idList = pathwayLiteratureReference.containsKey(id) ? pathwayLiteratureReference.get(id) : new ArrayList<String>();
        	idList.add(pubmedId);
        	
        	pathwayLiteratureReference.put(id, idList);
        }
        reader.close();
        
		fileName = homeDir + "/biodb/reactome/pathwaySummationLiteratureReference.txt";
        System.out.println("\nReading summation literature references from " + fileName);
        HashMap<String, ArrayList<String>> pathwaySummationLiteratureReference = new HashMap<String, ArrayList<String>>();

        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine(); //skip header line
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	String id = datavalue[0];
        	String pubmedId = datavalue[1];
        	ArrayList<String> idList;
        	
        	idList = pathwaySummationLiteratureReference.containsKey(id) ? pathwaySummationLiteratureReference.get(id) : new ArrayList<String>();
        	idList.add(pubmedId);
        	
        	pathwaySummationLiteratureReference.put(id, idList);
        }
        reader.close();
        
        System.out.print("\nCreating pathways nodes ");
        HashMap<String, Node> pathway = new HashMap<String, Node>();
        
        Label PUBMED = DynamicLabel.label( "Pubmed" );
        Label GO = DynamicLabel.label( "Go" );
        Label MIRNA = DynamicLabel.label( "MiRNA" );
        Label PATHWAY = DynamicLabel.label( "Pathway" );
		
		try ( Transaction tx = graphDb.beginTx() ) {
	        for (String id : pathwayName.keySet()) {
	        	String name = pathwayName.get(id);
	        	String disease = (pathwayDisease.get(id) == null) ? "" : pathwayDisease.get(id);
	        	String summation = (pathwaySummation.get(id) == null) ? "" : pathwaySummation.get(id);
	        	ArrayList<String> literatureReference = pathwayLiteratureReference.get(id);
	        	ArrayList<String> summationLiteratureReference = pathwaySummationLiteratureReference.get(id);
	        	
	        	Node p = graphDb.createNode( PATHWAY );
	        	p.setProperty("pathwayId", id);
	        	p.setProperty("name", name);
	        	p.setProperty("disease", disease);
	        	p.setProperty("summation", summation);
	        	
	        	entryCounter++;
	        	
	        	pathway.put(id, p);
	        	
	        	if (literatureReference != null)
		        	for (String reference : literatureReference) {
		            	ResourceIterator<Node> it = graphDb.findNodes(PUBMED, "pubmedId", reference);
		            	Node citation;
		            	if (it.hasNext())
		            		citation = it.next();
		            	else {
		            		citation = graphDb.createNode( PUBMED );
		            		citation.setProperty("pubmedId", reference);
		            		entryCounter++;
		            	}
		            	
		            	p.createRelationshipTo(citation, RelationTypes.CITED_IN);
		        		edgeCounter++;
		        	}
	        	
	        	if (summationLiteratureReference != null)
		        	for (String reference : summationLiteratureReference) {
		        		ResourceIterator<Node> it = graphDb.findNodes(PUBMED, "pubmedId", reference);
		            	Node citation;
		            	if (it.hasNext())
		            		citation = it.next();
		            	else {
		            		citation = graphDb.createNode( PUBMED );
		            		citation.setProperty("pubmedId", reference);
		            		entryCounter++;
		            	}
		            	
		            	p.createRelationshipTo(citation, RelationTypes.CITED_IN);
		        		edgeCounter++;
		        	}
	
	        	entryCounter++;
	            if (entryCounter % 100 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
	        }
			tx.success();
		}
		reader.close();
        
		fileName = homeDir + "/biodb/reactome/pathway2go.txt";
        System.out.print("\n\nCreating pathway to GO relations ");

        reader = new BufferedReader(new FileReader(fileName));
        line = reader.readLine(); //skip header line
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String id = datavalue[0];
	        	String goId = datavalue[1];
	
	        	Node p = pathway.get(id);
	        	Node g = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(GO, "goId", goId);
	        	if (it.hasNext())
	        		g = it.next();
	        	
	        	if ((p != null) && (g != null)) {
	        		g.createRelationshipTo(p, RelationTypes.ANNOTATES);
	 
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
			tx.success();
        }
        reader.close();
        
		fileName = homeDir + "/biodb/reactome/miRBase2Reactome_All_Levels.txt";
        System.out.print("\n\nCreating miRBase to pathway relations ");

        reader = new BufferedReader(new FileReader(fileName));
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String accessionId = datavalue[0];
	        	String pathwayId = datavalue[1];
	
	        	if (accessionId.startsWith("miR"))
	        		accessionId = "MI" + accessionId.substring(3);
	        	
	        	Node m = null;
	        	Node p = pathway.get(pathwayId);
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA, "accession", accessionId);
	        	if (it.hasNext())
	        		m = it.next();
	        	
	        	if ((p != null) && (m != null)) {
	        		m.createRelationshipTo(p, RelationTypes.MIRNA2PATHWAY);
	
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
			tx.success();
        }
        reader.close();

        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + entryCounter + " vertices and " + edgeCounter + " edges in " + timeConversion(stopTime));

        reader.close();

		graphDb.shutdown();
	}
}
