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

public class _16_miRNAtargetsGainBySNPsImport {

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

		String fileName = homeDir + "/biodb/miRNASNP/miRNA_targets_gain_by_SNPs_in_seed_regions.txt";
		String line;
		int entryCounter = 0;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting miRNA target gain by SNPs in seed regions from " + fileName + " ");
        
        Label MIRNA_SNP = DynamicLabel.label( "MiRNAsnp" );
        Label INTERACTION = DynamicLabel.label( "Interaction" );
        Label GENE = DynamicLabel.label( "Gene" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	//String id = datavalue[0];
	        	String gene_id = datavalue[1];
	        	String transcriptId = datavalue[2];
	        	//String mirAccession = datavalue[3];
	        	String SNPid = datavalue[4];
	        	
	        	int miRstart = Integer.valueOf(datavalue[5]);
	        	int miRend = Integer.valueOf(datavalue[6]);
	        	double energy = Double.valueOf(datavalue[7]);
	        	double snpEnergy = Double.valueOf(datavalue[8]);

	        	String basePair = datavalue[9];
	        	
	        	double geneAve = Double.NaN;
	        	if ((datavalue[10] != null) && (!datavalue[10].equalsIgnoreCase("null")))
	        		geneAve = Double.valueOf(datavalue[10]);
	        	
	        	//String geneId = datavalue[11];

	        	double mirnaAve = Double.NaN;
	        	if ((datavalue[12] != null) && (!datavalue[12].equalsIgnoreCase("null")))
	        		mirnaAve = Double.valueOf(datavalue[12]);

	        	//String mirnaId = datavalue[13];

	        	Node miRNASNP = null;
	        	Node gene = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA_SNP, "SNPid", SNPid);
	        	if (it.hasNext()) {
	        		miRNASNP = it.next();
	        		
	        		it = graphDb.findNodes(GENE, "nomenclatureAuthoritySymbol", gene_id);
	        		if (it.hasNext())
	        			gene = it.next();
	        		
	        		if ((miRNASNP != null) && (gene != null)) {
	        			Node interaction = graphDb.createNode( INTERACTION );
	                	interaction.setProperty("extTranscriptId", transcriptId);
	                	interaction.setProperty("mirStart", miRstart);
	                	interaction.setProperty("mirEnd", miRend);
	                	interaction.setProperty("energy", energy);
	                	interaction.setProperty("snpEnergy", snpEnergy);
	                	interaction.setProperty("basePair", basePair);
	                	interaction.setProperty("geneAve", geneAve);
	                	interaction.setProperty("mirnaAve", mirnaAve);
	                	interaction.setProperty("database", "miRNASNP");

	        			entryCounter++;
	        			
	        			interaction.createRelationshipTo(miRNASNP, RelationTypes.INTERACTING_SNP);
	        			interaction.createRelationshipTo(gene, RelationTypes.INTERACTING_GENE);
	                	edgeCounter += 2;

	                    if (entryCounter % 12500 == 0) {
	                    	System.out.print("."); System.out.flush();
	                    }
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
