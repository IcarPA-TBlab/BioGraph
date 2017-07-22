package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class Gene2Go extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws IOException {
		String line;
        
	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting gene-go associations from " + fileName + " ");
        
        // skip first line
        reader.readLine();
        
        Label GENE = DynamicLabel.label( "Gene" );
        Label GO = DynamicLabel.label( "Go" );
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {
	        	String datavalue[] = line.split("\t");
	        	
	        	String taxId = datavalue[0];
	        	if (!taxId.equals("9606"))
	        		continue;
	
	        	String geneId = datavalue[1];
	        	String goId = datavalue[2];
	        	String evidence = datavalue[3];
	        	String qualifier = datavalue[4];
	        	//String goTerm = datavalue[5];
	        	//String pubmedIds = datavalue[6];
	        	String category = datavalue[7];
	
	        	Node gene = null;
	        	Node go = null;
	
	        	ResourceIterator<Node> it = graphDb.findNodes( GENE, "geneId", geneId );
	        	
	        	if (it.hasNext()) {
	        		gene = it.next();
	        		
	        		it = graphDb.findNodes( GO, "goId", goId );
	        		if (it.hasNext())
	        			go = it.next();
	        	}
	        	
	        	if ((gene != null) && (go != null)) {
	            	edgeCounter++;
	            	
	            	Relationship association = go.createRelationshipTo(gene, RelationTypes.ANNOTATES);
	            	association.setProperty("evidence", evidence);
	            	association.setProperty("qualifier", qualifier);
	            	association.setProperty("category", category);
	        	}
	        	
	            if (edgeCounter % 5000 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
	        }
	        tx.success();
        }
        
        reader.close();
	}
}
