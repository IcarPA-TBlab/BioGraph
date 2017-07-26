package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class Reactome extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        String line;
        
        System.out.print("\nReading homo sapiens Reactome entries from " + fileName + " ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        Label PATHWAY = DynamicLabel.label( "Pathway" );
        
		try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String id = datavalue[0];
	        	String name = datavalue[1];
	        	String summation = datavalue[2];
	        	
	        	Node p = graphDb.createNode( PATHWAY );
	        	p.setProperty("pathwayId", id);
	        	p.setProperty("name", name);
	        	p.setProperty("summation", summation);
	        	
	        	entryCounter++;
	            if (entryCounter % 100 == 0) {
	            	System.out.print("."); System.out.flush();
	            }
	        }
			tx.success();
		}
        reader.close();
	}
}
