package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _17_miRTarBaseHSAImport {

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

		String fileName = homeDir + "/biodb/hsa_MTI.txt";
		String line;
		int entryCounter = 0;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting miRTarBase interactions from " + fileName + " ");
        
        Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
        Label INTERACTION = DynamicLabel.label( "Interaction" );
        Label GENE = DynamicLabel.label( "Gene" );
        Label PUBMED = DynamicLabel.label( "Pubmed" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	//String mirTarBaseId = datavalue[0];
	        	String mirna = datavalue[1];
	        	//String speciesTarget = datavalue[2];
	        	String targetGene = datavalue[3];
	        	//String geneId = datavalue[4];
	         	//String speciesTargetGene = datavalue[5];
	        	String experiments = datavalue[6];
	        	String supportType = datavalue[7];
	        	String pmid = datavalue[8];

	        	Node miRNA = null;
	        	Node gene = null;
	        	Node pubmed = null;

	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA_MATURE, "product", mirna);
	        	
	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	        		it = graphDb.findNodes(GENE, "nomenclatureAuthoritySymbol", targetGene);
	        		if (it.hasNext())
	        			gene = it.next();
	        	}
	        	
	        	if ((miRNA != null) && (gene != null)) {
	        		Node interaction = graphDb.createNode( INTERACTION );
	        		interaction.setProperty("experiments", experiments);
	        		interaction.setProperty("supportType", supportType);
	        		interaction.setProperty("database", "miRTarBase");	        		
	            	entryCounter++;
	        		
	            	interaction.createRelationshipTo(miRNA, RelationTypes.INTERACTING_MIRNA);
	            	interaction.createRelationshipTo(gene, RelationTypes.INTERACTING_GENE);
	            	edgeCounter += 2;

	            	it = graphDb.findNodes(PUBMED, "pubmedId", pmid);
	            	if (it.hasNext()) {
	            		pubmed = it.next();
	            	} else {
	            		pubmed = graphDb.createNode( PUBMED );
	            		pubmed.setProperty("pubmedId", pmid);
	            		entryCounter++;
	            	}
	            	
	            	interaction.createRelationshipTo(pubmed, RelationTypes.CITED_IN);
	            	edgeCounter++;
	            	
	                if (entryCounter % 25000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        tx.success();
		}
        
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + entryCounter + " vertices and "+ edgeCounter + " edges in " + timeConversion(stopTime));

        reader.close();

		graphDb.shutdown();
	}
	
}
