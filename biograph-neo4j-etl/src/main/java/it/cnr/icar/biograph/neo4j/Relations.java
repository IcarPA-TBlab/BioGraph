package it.cnr.icar.biograph.neo4j;

import org.neo4j.graphdb.RelationshipType;

public class Relations {

	public enum RelationTypes implements RelationshipType
	{
	    CITED_IN,
	    IS_A,
	    REGULATES,
	    NEGATIVELY_REGULATES,
	    POSITIVELY_REGULATES,
	    PART_OF,
	    HAS_PART,
	    ANNOTATES,
	    PRECURSOR_OF,
	    INTERACTING_GENE,
	    INTERACTING_MIRNA,
	    INTERACTING_SNP,
	    CANCER2MIRNA,
	    REFERS_TO,
	    SYNONYM_OF,
	    CODING,
	    HAS_SNP,
	    MIRNA2PATHWAY,
	    CONTAINS
	}
	
}
