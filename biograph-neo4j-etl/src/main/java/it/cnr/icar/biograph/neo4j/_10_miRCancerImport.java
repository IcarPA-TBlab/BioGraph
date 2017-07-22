package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _10_miRCancerImport {

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
        String fileName = homeDir + "/biodb/miRCancerSeptember2015.txt";
        
        System.out.print("\nImporting miRCancer from " + fileName + " ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        Label CANCER = DynamicLabel.label( "Cancer" );
        Label MIRNA = DynamicLabel.label( "MiRNA" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String mirId = datavalue[0];
	        	String cancerName = datavalue[1];
	        	String cancerProfile = datavalue[2];
	        	//String pubmedTitle = datavalue[3];
	
	        	Node miRNA = null;
	        	Node cancer = null;
	
	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA, "name", mirId);
	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	                it = graphDb.findNodes(CANCER, "name", cancerName);
	        		if (it.hasNext()) {
	        			cancer = it.next();
	        		} else {
	        			cancer = graphDb.createNode(CANCER);
	        			cancer.setProperty("name", cancerName);
	        			entryCounter++;
	        		}
	        		
	        		if ((miRNA != null) && (cancer != null)) {
	        			Relationship association = cancer.createRelationshipTo(miRNA, RelationTypes.CANCER2MIRNA);
	                	association.setProperty("profile", cancerProfile);
	            		edgeCounter++;
	        		}
	
	        		if (entryCounter % 500 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	    	tx.success();
        }

        	
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + entryCounter + " vertices and " + edgeCounter + " edges in " + timeConversion(stopTime));

        reader.close();

		graphDb.shutdown();
	}
	
}
