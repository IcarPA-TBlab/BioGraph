package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class SNPTargets extends Importer {
	
	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

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
        reader.close();
	}	
}
