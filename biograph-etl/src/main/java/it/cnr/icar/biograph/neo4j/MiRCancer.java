package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class MiRCancer extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
        String line;
        
        System.out.print("\nImporting miRCancer from " + fileName + " ");

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        Label CANCER = DynamicLabel.label( "Cancer" );
        Label MIRNA = DynamicLabel.label( "MiRNA" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String mirId = datavalue[0];
	        	String cancerName = datavalue[1];
	        	String cancerProfile = datavalue[2];
	        	//String pubmedTitle = datavalue[3];
	
	        	Node miRNA = null;
	        	Node cancer = null;
	
	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA, "name", mirId);
	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	                it = graphDb.findNodes(CANCER, "name", cancerName);
	        		if (it.hasNext()) {
	        			cancer = it.next();
	        		} else {
	        			cancer = graphDb.createNode(CANCER);
	        			cancer.setProperty("name", cancerName);
	        			entryCounter++;
	        		}
	        		
	        		if ((miRNA != null) && (cancer != null)) {
	        			Relationship association = cancer.createRelationshipTo(miRNA, RelationTypes.CANCER2MIRNA);
	                	association.setProperty("profile", cancerProfile);
	            		edgeCounter++;
	        		}
	
	        		if (entryCounter % 500 == 0) {
	                	System.out.print("."); System.out.flush();
	                }
	        	}
	        }
	    	tx.success();
        }

        reader.close();
	}
	
}
