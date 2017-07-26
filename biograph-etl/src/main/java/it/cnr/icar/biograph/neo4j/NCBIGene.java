package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class NCBIGene extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws IOException {
		String line;

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting NCBI gene info entries from " + fileName + " ");
        
        // skip first line
        reader.readLine();
        
        Label GENE = DynamicLabel.label( "Gene" );

        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {
	        	String datavalue[] = line.split("\t");
	        	
	        	//String taxId = datavalue[0];
	        	//if (!taxId.equals("9606")) continue;
	        	
	        	String geneId = datavalue[1];
	        	//String symbol = datavalue[2];
	        	String locusTag = datavalue[3];
	        	//String synonyms = datavalue[4];
	        	//String dbXrefs = datavalue[5];
	        	String chromosome = datavalue[6];
	        	String mapLocation = datavalue[7];
	        	String description = datavalue[8];
	        	String geneType = datavalue[9];
	        	String nomenclatureAuthoritySymbol = datavalue[10];
	        	String nomenclatureAuthorityFullName = datavalue[11];
	        	String nomenclatureStatus = datavalue[12];
	        	String otherDesignations = datavalue[13];
	        	//String modificationDate = datavalue[14];
	        	
	        	Node gene = graphDb.createNode( GENE );
	        	gene.setProperty("geneId", geneId);
	        	gene.setProperty("locusTag", locusTag);
	        	gene.setProperty("chromosome", chromosome);
	        	gene.setProperty("mapLocation", mapLocation);
	        	gene.setProperty("description", description);
	        	gene.setProperty("type", geneType);
	        	gene.setProperty("nomenclatureAuthoritySymbol", nomenclatureAuthoritySymbol);
	        	gene.setProperty("nomenclatureAuthorityFullName", nomenclatureAuthorityFullName);
	        	gene.setProperty("nomenclatureStatus", nomenclatureStatus);
	        	gene.setProperty("otherDesignations", otherDesignations);

	        	entryCounter++;
	
	            if (entryCounter % 10000 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
	        }
        	tx.success();
        }
        
        reader.close();
	}
}
