package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _12_Uniprot2PathwayImport {

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

		String fileName = homeDir + "/biodb/reactome/uniprot2pathway.txt";
		String line;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting protein to pathway associations from " + fileName + " ");
        
        //HashMap<String, Integer> noProt = new HashMap<String, Integer>();
        
        Label PROTEIN_NAME = DynamicLabel.label( "ProteinName" );
        Label PATHWAY = DynamicLabel.label( "Pathway" );
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String uniprotId = datavalue[0];
	        	String pathwayId = datavalue[1];
	
	        	Node protein = null;
	        	Node pathway = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(PROTEIN_NAME, "name", uniprotId);
	
	        	if (it.hasNext())
	        		protein = it.next().getSingleRelationship(RelationTypes.REFERS_TO, Direction.OUTGOING).getEndNode();       		
				else {
					/*
					Integer c = noProt.getOrDefault(uniprotId, 0);
					c++;
					noProt.put(uniprotId, c);
					*/
				}
	
				it = graphDb.findNodes(PATHWAY, "pathwayId", pathwayId);
				if (it.hasNext())
					pathway = it.next();
				
				if ((protein != null) && (pathway != null)) {
					pathway.createRelationshipTo(protein, RelationTypes.CONTAINS);
					edgeCounter++;
	
					if (edgeCounter % 5000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
				}
	
	        }
	        tx.success();
        }

        
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + edgeCounter + " edges in " + timeConversion(stopTime));

        reader.close();

		graphDb.shutdown();
	}
	
}
