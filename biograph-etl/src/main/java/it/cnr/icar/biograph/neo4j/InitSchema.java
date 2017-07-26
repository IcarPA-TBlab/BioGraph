package it.cnr.icar.biograph.neo4j;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;

public class InitSchema {
	public static void init(GraphDatabaseService graphDb) {	
		/*
		Label PUBMED = DynamicLabel.label( "Pubmed" );
		try ( Transaction tx = graphDb.beginTx() ) {
			graphDb.schema().indexFor( PUBMED ).on( "pubmedId" ).create();
			tx.success();
		}
		*/

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
	}
}
