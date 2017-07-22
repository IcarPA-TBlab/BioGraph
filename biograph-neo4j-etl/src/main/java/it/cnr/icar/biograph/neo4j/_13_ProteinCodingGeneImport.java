package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class _13_ProteinCodingGeneImport {

	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	private static String timeConversion(long seconds) {

	    final int MINUTES_IN_AN_HOUR = 60;
	    final int SECONDS_IN_A_MINUTE = 60;

	    long minutes = seconds / SECONDS_IN_A_MINUTE;
	    seconds -= minutes * SECONDS_IN_A_MINUTE;

	    long hours = minutes / MINUTES_IN_AN_HOUR;
	    minutes -= hours * MINUTES_IN_AN_HOUR;

	    return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}
	
	static String DB_PATH = "/biograph.db";
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String homeDir = System.getProperty("user.home");
		File dbPath = new File(homeDir + DB_PATH);
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook( graphDb );

		String fileName = homeDir + "/biodb/protein-coding_gene.txt";
		String line;
		int entryCounter = 0;
		int edgeCounter = 0;
        long startTime = System.currentTimeMillis();

	    BufferedReader reader = new BufferedReader(new FileReader(fileName));

        System.out.print("\nImporting proteing coding to gene associations from " + fileName + " ");
        
        Label GENE = DynamicLabel.label( "Gene" );
        Label GENE_NAME = DynamicLabel.label( "GeneName" );
        Label PROTEIN_NAME = DynamicLabel.label( "ProteinName" );
        
        // skip first line
        reader.readLine();
        
        try ( Transaction tx = graphDb.beginTx() ) {
	        while ((line = reader.readLine()) != null) {        	
	        	String datavalue[] = line.split("\t");
	        	
	        	String hgncId = datavalue[0];
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
	        	String dateModified = datavalue[17];
	        	*/
	        	String entrezId = datavalue[18];
	        	String ensemblGeneId = datavalue[19];
	        	/*
	        	String vegaId = datavalue[20];
	        	String ucscId = datavalue[21];
	        	String ena = datavalue[22];
	        	 */
	        	String refseqAccession = datavalue[23];
	        	if (datavalue.length < 25)
	        		continue;
	        	//String ccdsId = datavalue[24];
	        	if (datavalue.length < 26)
	        		continue;
	        	String uniprotIds = datavalue[25];
	        	
	        	/*
	        	if (datavalue.length < 27)
	        		continue;
	        	String pubmedId = datavalue[26];
	        	if (datavalue.length < 28)
	        		continue;
	        	String mgdId = datavalue[27];
	        	if (datavalue.length < 29)
	        		continue;
	        	String rgdId = datavalue[28];
	        	if (datavalue.length < 30)
	        		continue;
	        	String lsdb = datavalue[29];
	        	if (datavalue.length < 31)
	        		continue;
	        	String cosmic = datavalue[30];
	        	if (datavalue.length < 32)
	        		continue;
	        	String omimId = datavalue[31];
	        	if (datavalue.length < 33)
	        		continue;
	        	String mirbase = datavalue[32];
	        	String homeodb = datavalue[33];
	        	if (datavalue.length < 35)
	        		continue;
	        	String snornabase = datavalue[34];
	        	String bioparadigmsSlc = datavalue[35];
	        	if (datavalue.length < 37)
	        		continue;
	        	String orphanet = datavalue[36];
	        	if (datavalue.length < 38)
	        		continue;
	        	String pseudogene = datavalue[37];
	        	if (datavalue.length < 39)
	        		continue;
	        	String hordeId = datavalue[38];
	        	if (datavalue.length < 40)
	        		continue;
	        	String merops = datavalue[39];
	        	if (datavalue.length < 41)
	        		continue;
	        	String imgt = datavalue[40];
	        	String iuphar = datavalue[41];
	        	if (datavalue.length < 43)
	        		continue;
	        	String kznfGeneCatalog = datavalue[42];
	        	if (datavalue.length < 44)
	        		continue;
	        	String mamitTrnadb = datavalue[43];
	        	String cd = datavalue[44];
	        	if (datavalue.length < 46)
	        		continue;
	        	String lncrnadb = datavalue[45];
	        	String enzymeId = datavalue[46];
	        	if (datavalue.length < 48)
	        		continue;
	        	String intermediateFilamentDb = datavalue[47];
	        	*/
	
	        	if (!entrezId.equals("")) {
	        		Node gene = null;
	        		
	        		ResourceIterator<Node> it = graphDb.findNodes(GENE, "geneId", entrezId);
	        		if (it.hasNext())
	        			gene = it.next();
	
	        		if (gene != null) {            		
	        			Node ncbi = graphDb.createNode( GENE_NAME );
	        			ncbi.setProperty("symbol", entrezId);
	        			ncbi.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	
	        			entryCounter++;
	        			edgeCounter++;
	        			
	        			String entrezSymbol = (String) gene.getProperty("nomenclatureAuthoritySymbol");
	        			if ((entrezSymbol != null) && (!entrezSymbol.equals("-"))) {
		        			Node symbol = graphDb.createNode( GENE_NAME );
		        			symbol.setProperty("symbol", entrezSymbol);
		        			symbol.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	
		        			entryCounter++;
		        			edgeCounter++;
		        		}
	
	        			if ((hgncId != null) && (!hgncId.equals(""))) {
	        				Node hgnc = graphDb.createNode( GENE_NAME );
	        				hgnc.setProperty("symbol", hgncId);
	            			hgnc.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	            			
	            			entryCounter++;
	            			edgeCounter++;
	            		}
	        			
	            		if ((ensemblGeneId != null) && (!ensemblGeneId.equals(""))) {
	            			Node ensembl = graphDb.createNode( GENE_NAME );
	            			ensembl.setProperty("symbol", ensemblGeneId);
	            			ensembl.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	            			
	            			entryCounter++;
	            			edgeCounter++;
	            		}
	            		
	            		if ((refseqAccession != null) && (!refseqAccession.equals(""))) {
	            			if (refseqAccession.contains("|")) {
	            				String refseqId[] = refseqAccession.split("|");
	            				for (int i=0; i<refseqId.length; i++) {
	            					Node refseq = graphDb.createNode( GENE_NAME );
	            					refseq.setProperty("symbol", refseqId[i]);
	    	            			refseq.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	    	            			
	    	            			entryCounter++;
	    	            			edgeCounter++;
	            				}
	            			} else {
		            			Node refseq = graphDb.createNode( GENE_NAME );
		            			refseq.setProperty("symbol", refseqAccession);
		            			refseq.createRelationshipTo(gene, RelationTypes.SYNONYM_OF);
	
		            			entryCounter++;
		            			edgeCounter++;
	            			}
	            		}
	            		
	                	if (!uniprotIds.equals("")) {
	                		if (uniprotIds.contains("|")) {
	                			String uniprotId[] = uniprotIds.split("|");
	                			
	                    		for (int i=0; i<uniprotId.length; i++) {
	                    			it = graphDb.findNodes(PROTEIN_NAME, "name", uniprotId[i]);
	                    			if (it.hasNext()) {
	                    				Node protein = it.next().getSingleRelationship(RelationTypes.REFERS_TO, Direction.OUTGOING).getEndNode();                    				
	                    				gene.createRelationshipTo(protein, RelationTypes.CODING);
	                    				edgeCounter++;
	                    			}
	                    		}
	                		} else {
	                			it = graphDb.findNodes(PROTEIN_NAME, "name", uniprotIds);
	                			if (it.hasNext()) {
	                				Node protein = it.next().getSingleRelationship(RelationTypes.REFERS_TO, Direction.OUTGOING).getEndNode();                    				
	                				gene.createRelationshipTo(protein, RelationTypes.CODING);
	                				edgeCounter++;
	                			}
	                		}
	                	}
	
	                    if (entryCounter % 1000 == 0) {
	                    	System.out.print("."); System.out.flush();
	                    }
	        		}
	        	}
	        }
	        tx.success();
		}
        reader.close();

		fileName = homeDir + "/biodb/non-coding_RNA.txt";
	    reader = new BufferedReader(new FileReader(fileName));
		
        System.out.print("\n\nImporting non-coding associations from " + fileName + " ");
        
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
        
        long stopTime = (System.currentTimeMillis()-startTime)/1000;
        System.out.println("\n\nCreated " + edgeCounter + " edges in " + timeConversion(stopTime));

        reader.close();

		graphDb.shutdown();
	}
	
}
