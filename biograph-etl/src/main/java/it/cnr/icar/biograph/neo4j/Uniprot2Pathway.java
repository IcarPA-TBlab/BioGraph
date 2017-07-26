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

public class Uniprot2Pathway extends Importer {
	
	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting protein to pathway associations from " + fileName + " ");
        
        //HashMap<String, Integer> noProt = new HashMap<String, Integer>();
        
        Label PROTEIN = DynamicLabel.label( "Protein" );
        Label PATHWAY = DynamicLabel.label( "Pathway" );
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String uniprotId = datavalue[0];
	        	String pathwayId = datavalue[1];
	
	        	Node protein = null;
	        	Node pathway = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(PROTEIN, "name", uniprotId);
	        	if (it.hasNext()) {
	        		protein = it.next();
	        		
					it = graphDb.findNodes(PATHWAY, "pathwayId", pathwayId);
					if (it.hasNext())
						pathway = it.next();	        		
	        	}
				
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
        reader.close();
	}
}
