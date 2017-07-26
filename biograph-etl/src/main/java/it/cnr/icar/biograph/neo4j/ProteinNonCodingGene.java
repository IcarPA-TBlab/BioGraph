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

public class ProteinNonCodingGene extends Importer {

	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        Label GENE = DynamicLabel.label( "Gene" );
        Label GENE_NAME = DynamicLabel.label( "GeneName" );

        System.out.print("\nImporting non-coding associations from " + fileName + " ");
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	    		Node gene = null;
	
	        	String datavalue[] = line.split("\t");
	        	
	        	String hgncId = datavalue[0];
	        	String symbol = datavalue[1];
	        	
	    		if ((symbol != null) && (!symbol.equals(""))) {        		
	        		ResourceIterator<Node> it = graphDb.findNodes(GENE, "geneId", symbol);
	        		if (it.hasNext())
	        			gene = it.next();
	        	}
	    		if (gene == null)
	    			continue;
	    		
				Node ncbi = graphDb.createNode( GENE_NAME );
				ncbi.setProperty("symbol", symbol);
				ncbi.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	
				entryCounter++;
				edgeCounter++;
	
	    		if (!hgncId.equals("")) {
	    			Node hgnc = graphDb.createNode( GENE_NAME );
	    			hgnc.setProperty("symbol", hgncId);
	    			hgnc.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	
	    			entryCounter++;
	    			edgeCounter++;
	    		}
	
	        	/*
	    		String name = datavalue[2];
	        	String locusGroup = datavalue[3];
	        	String locusType = datavalue[4];
	        	String status = datavalue[5];
	        	String location = datavalue[6];
	        	String locationSortable = datavalue[7];
	        	String aliasSymbol = datavalue[8];
	        	String aliasName = datavalue[9];
	        	String prevSymbol = datavalue[10];
	        	String prevName = datavalue[11];
	        	String geneFamily = datavalue[12];
	        	String geneFamilyId = datavalue[13];
	        	String dateApprovedReserved = datavalue[14];
	        	String dateSymbolChanged = datavalue[15];
	        	String dateNameChanged = datavalue[16];
	        	String dateModifiedEntrezId = datavalue[17];
	        	*/
	
	        	if (datavalue.length < 19)
	        		continue;
	        	String ensemblGeneId = datavalue[18];
	    		if (!ensemblGeneId.equals("")) {
	    			Node ensembl = graphDb.createNode( GENE_NAME );
	    			ensembl.setProperty("symbol", ensemblGeneId);
	    			ensembl.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	    			
	    			entryCounter++;
	    			edgeCounter++;
	    		}
	
	        	if (datavalue.length < 20)
	        		continue;
	        	//String vegaId = datavalue[19];
	        	//String ucscId = datavalue[20];
	        	if (datavalue.length < 22)
	        		continue;
	        	//String ena = datavalue[21];
	        	
	        	if (datavalue.length < 23)
	        		continue;
	        	String refseqAccession = datavalue[22];
	    		if (!refseqAccession.equals("")) {
	    			Node refseq = graphDb.createNode( GENE_NAME );
	    			refseq.setProperty("symbol", refseqAccession);
	    			refseq.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	    			
	    			entryCounter++;
	    			edgeCounter++;
	    		}
	
	        	/*
	        	if (datavalue.length < 24)
	        		continue;
	        	//String ccdsId = datavalue[23];
	        	
	        	if (datavalue.length < 25)
	        		continue;
	        	
	        	String uniprotIds = datavalue[24];
	        	if (!uniprotIds.equals(""))
	        		System.out.println(uniprotIds);
	        	String pubmedId = datavalue[25];
	        	if (datavalue.length < 27)
	        		continue;
	        	String mgdId = datavalue[26];
	        	if (datavalue.length < 28)
	        		continue;
	        	String rgdId = datavalue[27];
	        	if (datavalue.length < 29)
	        		continue;
	        	String lsdbcosmic = datavalue[28];
	        	if (datavalue.length < 30)
	        		continue;
	        	String omimId = datavalue[29];
	        	if (datavalue.length < 31)
	        		continue;
	        	String mirbase = datavalue[30];
	        	if (datavalue.length < 32)
	        		continue;
	        	String homeodb = datavalue[31];
	        	if (datavalue.length < 33)
	        		continue;
	        	String snornabase = datavalue[32];
	        	if (datavalue.length < 34)
	        		continue;
	        	String bioparadigmsSlc = datavalue[33];
	        	String orphanet = datavalue[34];
	        	if (datavalue.length < 36)
	        		continue;
	        	String pseudogeneOrg = datavalue[35];
	        	String hordeId = datavalue[36];
	        	if (datavalue.length < 38)
	        		continue;
	        	String merops = datavalue[37];
	        	if (datavalue.length < 39)
	        		continue;
	        	String imgt = datavalue[38];
	        	String iuphar = datavalue[39];
	        	String kznfGeneCatalog = datavalue[40];
	        	String mamitTrnadb = datavalue[41];
	        	String cd = datavalue[42];
	        	String lncrnadb = datavalue[43];
	        	if (datavalue.length < 45)
	        		continue;
	        	String enzymeId = datavalue[44];
	        	String intermediateFilamentDb = datavalue[45];
	        	*/   	
	        }
	        tx.success();
		}
        reader.close();
	}	
}
