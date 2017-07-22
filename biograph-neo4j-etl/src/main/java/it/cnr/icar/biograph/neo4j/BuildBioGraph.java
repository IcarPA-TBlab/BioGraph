package it.cnr.icar.biograph.neo4j;

import java.io.File;
import java.util.HashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class BuildBioGraph {

	static String DB_PATH = "/biograph.graphdb";
	static String SRC_PATH = "/datasources";
	
	static HashMap<String, String> sourcesMap = null;
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
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

	public static String missingSources(String srcPath) {
		String missing = null;
		
		for (String id : sourcesMap.keySet()) {
			String filename = srcPath + sourcesMap.get(id);			
			File sourcefile = new File(filename);
			
			if (!sourcefile.exists())
				missing = id;
		}
		
		return missing;
	}
	
	public static void initSourcesMap() {
		sourcesMap = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
		{
		    put("GENES",		"Homo_sapiens.gene_info");
		    put("GO",			"go_daily-termdb.obo-xml");
		    put("GENE2GO",		"gene2go");
		    put("MIRBASE",		"miRNA.txt");
		    put("MIRANDA",		"human_predictions_S_C_aug2010.txt");
		    put("PATHWAYS", 	"pathway2summation.txt");
		    put("PATHWAYS2GO",  "pathway2go.txt");
		    put("PATHWAYS2MIR", "miRBase2Reactome_All_Levels.txt");
		    put("MIRCANCER", 	"miRCancerDecember2016.txt");
		    put("UNIPROT",		"uniprot_sprot.xml");
		    put("UNIPROT2PATH",	"uniprot2pathway.txt");
		    put("HGNC",			"hgnc_complete_set.txt");
		    put("IDMAP", 		"HUMAN_9606_idmapping_selected.tab");
		    put("CODING",		"protein-coding_gene.txt");
		    put("NONCODING",	"non-coding_RNA.txt");
		    put("MIRNASNP", 	"snp_in_human_miRNA_seed_region.txt");
		    put("SNPTARGETS", 	"miRNA_targets_gain_by_SNPs_in_seed_regions.txt");
		    put("MIRTARBASE",  	"hsa_MTI.txt");
		}};
	}
	
	public static void main(String[] args) throws Exception {
		String workDir = System.getProperty("user.dir");
		String dataSourcesPath = workDir + SRC_PATH + "/";
		
		File dbPath = new File(workDir + DB_PATH);
		
		initSourcesMap();
		
		String missing = missingSources(dataSourcesPath);
		if (missing != null) {
			System.err.println("\nDatasource file " + dataSourcesPath + sourcesMap.get(missing) + " is missing!");
			System.exit(1);
		}
		
		//deleteDirectory(dbPath);
		
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook( graphDb );
		
		//InitSchema.init(graphDb);

		/*
		new NCBIGene().importData(graphDb, dataSourcesPath + sourcesMap.get("GENES"));
		new GeneOntology().importData(graphDb, dataSourcesPath + sourcesMap.get("GO"));
		new Gene2Go().importData(graphDb, dataSourcesPath + sourcesMap.get("GENE2GO"));
		new MiRBase().importData(graphDb, dataSourcesPath + sourcesMap.get("MIRBASE"));
		new Miranda().importData(graphDb, dataSourcesPath + sourcesMap.get("MIRANDA"));
		new Reactome().importData(graphDb, dataSourcesPath + sourcesMap.get("PATHWAYS"));
		new Reactome2Go().importData(graphDb, dataSourcesPath + sourcesMap.get("PATHWAYS2GO"));
		new Reactome2Mirna().importData(graphDb, dataSourcesPath + sourcesMap.get("PATHWAYS2MIR"));
		new MiRCancer().importData(graphDb, dataSourcesPath + sourcesMap.get("MIRCANCER"));
		new Uniprot().importData(graphDb, dataSourcesPath + sourcesMap.get("UNIPROT"));
		new Uniprot2Pathway().importData(graphDb, dataSourcesPath + sourcesMap.get("UNIPROT2PATH"));
		new HGNC().importData(graphDb, dataSourcesPath + sourcesMap.get("HGNC"));
		new UniprotIdMapping().importData(graphDb, dataSourcesPath + sourcesMap.get("IDMAP"));
		new ProteinCodingGene().importData(graphDb, dataSourcesPath + sourcesMap.get("CODING"));
		new ProteinNonCodingGene().importData(graphDb, dataSourcesPath + sourcesMap.get("NONCODING"));
		new MiRNASNP().importData(graphDb, dataSourcesPath + sourcesMap.get("MIRNASNP"));
		new SNPTargets().importData(graphDb, dataSourcesPath + sourcesMap.get("SNPTARGETS"));
		*/
		new MiRTarBase().importData(graphDb, dataSourcesPath + sourcesMap.get("MIRTARBASE"));
		
		graphDb.shutdown();
	}
}
