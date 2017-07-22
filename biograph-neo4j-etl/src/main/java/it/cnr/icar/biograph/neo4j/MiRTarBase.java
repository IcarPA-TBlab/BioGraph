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

public class MiRTarBase extends Importer {
	
	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting miRTarBase interactions from " + fileName + " ");
        
        Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
        Label INTERACTION = DynamicLabel.label( "Interaction" );
        Label GENE = DynamicLabel.label( "Gene" );
        //Label PUBMED = DynamicLabel.label( "Pubmed" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String mirTarBaseId = datavalue[0];
	        	String mirna = datavalue[1];
	        	//String speciesTarget = datavalue[2];
	        	String targetGene = datavalue[3];
	        	//String geneId = datavalue[4];
	         	//String speciesTargetGene = datavalue[5];
	        	String experiments = datavalue[6];
	        	String supportType = datavalue[7];
	        	//String pmid = datavalue[8];

	        	Node miRNA = null;
	        	Node gene = null;
	        	//Node pubmed = null;

	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA_MATURE, "product", mirna);
	        	
	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	        		it = graphDb.findNodes(GENE, "nomenclatureAuthoritySymbol", targetGene);
	        		if (it.hasNext())
	        			gene = it.next();
	        	}
	        	
	        	if ((miRNA != null) && (gene != null)) {
	        		Node interaction = graphDb.createNode( INTERACTION );
	        		interaction.setProperty("mirTarBaseId", mirTarBaseId);
	        		interaction.setProperty("experiments", experiments);
	        		interaction.setProperty("supportType", supportType);
	        		interaction.setProperty("database", "miRTarBase");	        		
	            	entryCounter++;
	        		
	            	interaction.createRelationshipTo(miRNA, RelationTypes.INTERACTING_MIRNA);
	            	interaction.createRelationshipTo(gene, RelationTypes.INTERACTING_GENE);
	            	edgeCounter += 2;

	            	/*
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
	            	*/
	            	
	                if (entryCounter % 25000 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	        tx.success();
		}
        reader.close();
	}
}
