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

public class Reactome2Go extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        String line;
        
        Label PATHWAY = DynamicLabel.label( "Pathway" );
        Label GO = DynamicLabel.label( "Go" );
        
        System.out.print("\n\nCreating pathway to GO relations ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        line = reader.readLine(); //skip header line
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String goId = datavalue[0];
	        	String id = datavalue[1];
	
	        	Node p = null;
	        	Node g = null;
	        	
	        	ResourceIterator<Node> it = graphDb.findNodes(PATHWAY, "pathwayId", id);
	        	if (it.hasNext()) {
	        		p = it.next();
	        		
	        		it = graphDb.findNodes(GO, "goId", goId);
		        	if (it.hasNext())
		        		g = it.next();
	        	}
	        	
	        	if ((p != null) && (g != null)) {
	        		g.createRelationshipTo(p, RelationTypes.ANNOTATES);
	 
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
