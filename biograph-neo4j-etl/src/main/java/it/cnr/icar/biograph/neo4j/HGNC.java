package it.cnr.icar.biograph.neo4j;

import java.io.BufferedReader;
import java.io.FileReader;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import it.cnr.icar.biograph.neo4j.Relations.RelationTypes;

public class HGNC extends Importer {

	@SuppressWarnings("resource")
	protected void importer(GraphDatabaseService graphDb, String fileName) throws Exception {
		String line;

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
	        	String symbol = datavalue[1];
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
	        	if (datavalue.length < 19)
	        		continue;
	        	String entrezId = datavalue[18];
	        	if (datavalue.length < 20)
	        		continue;
	        	String ensemblGeneId = datavalue[19];
	        	/*
	        	String vegaId = datavalue[20];
	        	String ucscId = datavalue[21];
	        	String ena = datavalue[22];
	        	 */
	        	if (datavalue.length < 24)
	        		continue;
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
	}	
}
