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

public class Reactome2Mirna extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        String line;
        
        Label PATHWAY = DynamicLabel.label( "Pathway" );
        Label MIRNA = DynamicLabel.label( "MiRNA" );
        
        System.out.print("\n\nCreating miRBase to pathway relations ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String accessionId = datavalue[0];
	        	String pathwayId = datavalue[1];
	
	        	if (accessionId.startsWith("miR"))
	        		accessionId = "MI" + accessionId.substring(3);
	        	
	        	Node m = null;
	        	Node p = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(PATHWAY, "pathwayId", pathwayId);
	        	if (it.hasNext()) {
	        		p = it.next();
	        		
	        		it = graphDb.findNodes(MIRNA, "accession", accessionId);
		        	if (it.hasNext())
		        		m = it.next();
	        	}
	        		        	
	        	if ((p != null) && (m != null)) {
	        		m.createRelationshipTo(p, RelationTypes.MIRNA2PATHWAY);
	
	        		edgeCounter++;
	                if (edgeCounter % 100 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
			tx.success();
        }
        reader.close();
	}
}
