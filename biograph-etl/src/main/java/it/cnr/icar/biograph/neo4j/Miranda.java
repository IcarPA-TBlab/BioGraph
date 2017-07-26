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

public class Miranda extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        String line;
        
        System.out.print("\nImporting human MiRNA-genes interactions from " + fileName + " ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        Label GENE = DynamicLabel.label( "Gene" );
        Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
        Label INTERACTION = DynamicLabel.label( "Interaction" );
        
        // skip first line
        reader.readLine();
        
        while ((line = reader.readLine()) != null) {        	
        	String datavalue[] = line.split("\t");
        	
        	String mirAccession = datavalue[0];
        	//String mirName = datavalue[1];
        	String geneId = datavalue[2];
        	//String geneSymbol = datavalue[3];
        	String transcriptId = datavalue[4];
        	String extTranscriptId = datavalue[5];
        	String mirAlignment = datavalue[6];
        	String alignment = datavalue[7];
        	String geneAlignment = datavalue[8];
        	int mirStart = Integer.valueOf(datavalue[9]);
        	int mirEnd = Integer.valueOf(datavalue[10]);
        	int geneStart = Integer.valueOf(datavalue[11]);
        	int geneEnd = Integer.valueOf(datavalue[12]);
        	String genomeCoordinates = datavalue[13];
        	double conservation = Double.valueOf(datavalue[14]);
        	int alignScore = Integer.valueOf(datavalue[15]);
        	int seedCat = Integer.valueOf(datavalue[16]);
        	double energy = Double.valueOf(datavalue[17]);
        	double mirSvrScore = Double.valueOf(datavalue[18]);

            try ( Transaction tx = graphDb.beginTx() ) {
	        	Node miRNA = null;
	        	Node gene = null;
	
	        	ResourceIterator<Node> it = graphDb.findNodes( MIRNA_MATURE, "accession", mirAccession );
	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	        		it = graphDb.findNodes( GENE, "geneId", geneId);
	        		if (it.hasNext())
	        			gene = it.next();
	        	}
	        	
	        	if ((miRNA != null) && (gene != null)) {
	        		Node interaction = graphDb.createNode( INTERACTION );
	        		interaction.setProperty("transcriptId", transcriptId);
	        		interaction.setProperty("extTranscriptId", extTranscriptId);
	        		interaction.setProperty("mirAlignment", mirAlignment);
	        		interaction.setProperty("alignment", alignment);
	        		interaction.setProperty("geneAlignment", geneAlignment);
	        		interaction.setProperty("mirStart", mirStart);
	        		interaction.setProperty("mirEnd", mirEnd);
	        		interaction.setProperty("geneStart", geneStart);
	        		interaction.setProperty("geneEnd", geneEnd);
	        		interaction.setProperty("genomeCoordinates", genomeCoordinates);
	        		interaction.setProperty("conservation", conservation);
	        		interaction.setProperty("alignScore", alignScore);
	        		interaction.setProperty("seedCat", seedCat);
	        		interaction.setProperty("energy", energy);
	        		interaction.setProperty("mirSvrScore", mirSvrScore);
	        		interaction.setProperty("database", "miRanda");
	        		
	        		interaction.createRelationshipTo(miRNA, RelationTypes.INTERACTING_MIRNA);
	        		interaction.createRelationshipTo(gene, RelationTypes.INTERACTING_GENE);
	        		
	        		entryCounter++;
	        		edgeCounter += 2;
	        		
	                if (entryCounter % 100000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	} else {
	        		//errorLog.add("NOT FOUND: " + mirAccession + " [line: " + entryCounter + "] " + gene );
	        	}
	        	tx.success();
            }
        }
        	
        reader.close();
	}
}
