package it.cnr.icar.biograph.neo4j;

import java.io.File;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class _00_InitSchema {

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
	
	static String DB_PATH = "/biograph.db";
	
	public static boolean deleteDirectory(File dir) { 
		if (dir.isDirectory()) { 
			File[] children = dir.listFiles(); 
			for (int i = 0; i < children.length; i++) { 
				boolean success = deleteDirectory(children[i]); 
				if (!success) 
					return false;
			}
		}
		return dir.delete();
	}

	public static void main(String[] args) {
		String homeDir = System.getProperty("user.home");
		File dbPath = new File(homeDir + DB_PATH);
		
		deleteDirectory(dbPath);
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook( graphDb );
		
		Label PUBMED = DynamicLabel.label( "Pubmed" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( PUBMED ).on( "pubmedId" ).create();
			tx.success();
		}

		Label GENE = DynamicLabel.label( "Gene" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( GENE ).on( "geneId" ).create();
			graphDb.schema().indexFor( GENE ).on( "locusTag" ).create();
			graphDb.schema().indexFor( GENE ).on( "chromosome" ).create();
			graphDb.schema().indexFor( GENE ).on( "mapLocation" ).create();
			graphDb.schema().indexFor( GENE ).on( "nomenclatureAuthoritySymbol" ).create();
			tx.success();
		}
		
		Label GO = DynamicLabel.label( "Go" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( GO ).on( "goId" ).create();
			tx.success();
		}
		
		Label MIRNA = DynamicLabel.label( "MiRNA" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( MIRNA ).on( "accession" ).create();
			graphDb.schema().indexFor( MIRNA ).on( "name" ).create();
			tx.success();
		}
		
		Label MIRNA_MATURE = DynamicLabel.label( "MiRNAmature" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( MIRNA_MATURE ).on( "accession" ).create();
			graphDb.schema().indexFor( MIRNA_MATURE ).on( "product" ).create();
			tx.success();
		}

		Label INTERACTION = DynamicLabel.label( "Interaction" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( INTERACTION ).on( "energy" ).create();
			graphDb.schema().indexFor( INTERACTION ).on( "snpEnergy" ).create();
			tx.success();
		}

		Label CANCER = DynamicLabel.label( "Cancer" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( CANCER ).on( "name" ).create();
			tx.success();
		}

		Label PROTEIN = DynamicLabel.label( "Protein" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( PROTEIN ).on( "name" ).create();
			tx.success();
		}

		Label PROTEIN_NAME = DynamicLabel.label( "ProteinName" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( PROTEIN_NAME ).on( "name" ).create();
			tx.success();
		}

		Label GENE_NAME = DynamicLabel.label( "GeneName" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( GENE_NAME ).on( "symbol" ).create();
			tx.success();
		}

		Label MIRNA_SNP = DynamicLabel.label( "MiRNAsnp" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( MIRNA_SNP ).on( "snpId" ).create();
			tx.success();
		}

        Label PATHWAY = DynamicLabel.label( "Pathway" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( PATHWAY ).on( "pathwayId" ).create();
			tx.success();
		}
		
		graphDb.shutdown();
	}
}
