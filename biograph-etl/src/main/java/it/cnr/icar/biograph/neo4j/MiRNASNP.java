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

public class MiRNASNP extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting SNPs in human miRNA seed region from " + fileName + " ");
        
        Label MIRNA_SNP = DynamicLabel.label( "MiRNAsnp" );
        Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	          	String datavalue[] = line.split("\t");
	        	
	        	String mirna = datavalue[0];
	        	String chr = datavalue[1];
	        	int miRstart = Integer.valueOf(datavalue[2]);
	        	int miRend = Integer.valueOf(datavalue[3]);
	        	String SNPid = datavalue[4];
	        	int lostNum = Integer.valueOf(datavalue[5]);
	        	int gainNum = Integer.valueOf(datavalue[6]);

	        	Node miRNA = null;

	        	ResourceIterator<Node> it = graphDb.findNodes(MIRNA_MATURE, "product", mirna);

	        	if (it.hasNext()) {
	        		miRNA = it.next();
	        		
	        		Node snp = graphDb.createNode( MIRNA_SNP );
	        		snp.setProperty("SNPid", SNPid);
	        		snp.setProperty("miRNA", mirna);
	        		snp.setProperty("chr", chr);
	        		snp.setProperty("miRstart", miRstart);
	        		snp.setProperty("miRend", miRend);
	        		snp.setProperty("lostNum", lostNum);
	        		snp.setProperty("gainNum", gainNum);
	        		entryCounter++;

	        		miRNA.createRelationshipTo(snp, RelationTypes.HAS_SNP);
	        		edgeCounter++;
	        	}
	        }
	        tx.success();
		}
        reader.close();
	}	
}
