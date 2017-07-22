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

public class _15_miRNASNPImport {

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

		String fileName = homeDir + "/biodb/miRNASNP/snp_in_human_miRNA_seed_region.txt";
		String line;
		int entryCounter = 0;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting SNPs in human miRNA seed region from " + fileName + " ");
        
        Label MIRNA_SNP = DynamicLabel.label( "MiRNAsnp" );
        Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	          	String datavalue[] = line.split("\t");
	        	
	        	String mirna = datavalue[0];
	        	String chr = datavalue[1];
	        	int miRstart = Integer.valueOf(datavalue[2]);
	        	int miRend = Integer.valueOf(datavalue[3]);
	        	String SNPid = datavalue[4];
	        	int lostNum = Integer.valueOf(datavalue[5]);
	        	int gainNum = Integer.valueOf(datavalue[6]);

	        	Node miRNA = null;

	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA_MATURE, "product", mirna);

	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	        		Node snp = graphDb.createNode( MIRNA_SNP );
	        		snp.setProperty("SNPid", SNPid);
	        		snp.setProperty("miRNA", mirna);
	        		snp.setProperty("chr", chr);
	        		snp.setProperty("miRstart", miRstart);
	        		snp.setProperty("miRend", miRend);
	        		snp.setProperty("lostNum", lostNum);
	        		snp.setProperty("gainNum", gainNum);
	        		entryCounter++;

	        		miRNA.createRelationshipTo(snp, RelationTypes.HAS_SNP);
	        		edgeCounter++;
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
