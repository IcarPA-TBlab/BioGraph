var express = require('express'),
	path = require('path'),
	http = require('http'),
	form = require('express-form'),
	Gremlin = require('gremlin'),
	utils = require('./utils');

var field = form.field;
var client = Gremlin.createClient(8182, 'localhost', { path: '/gremlin' });
//var client = Gremlin.createClient(9192, 'orient.pa.icar.cnr.it', { path: '/gremlin' });
var app = express();

// all environments
app.set('port', process.env.PORT || 3000);
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);

// development only
if ('development' === app.get('env')) {
  app.use(express.errorHandler());
}

var genesNumber = 0;
var query = "g.V().hasLabel('Gene').count()";
client.execute(query, (err, results) => {
	genesNumber = results;
	//console.log("genes: " + genesNumber);
});

// load pathway names for autocomplete
var pathwayNames = [];
var query = "g.V().hasLabel('Pathway').order().by('name').values('name')";
client.execute(query, (err, results) => {
	if (!err) {
		results.forEach(function (res){
			pathwayNames.push({
				name: res
			});
		});
		//console.log(pathwayNames);
	}
});

// load cancer names for autocomplete
var cancerNames = [];
query = "g.V().hasLabel('Cancer').order().by('name').values('name')";
client.execute(query, (err, results) => {
	if (!err) {
		results.forEach(function (res){
			cancerNames.push({
				name: res
			});
		});
		//console.log(cancerNames);
	}
});

//load go terms for autocomplete
var goTerms = [];
query = "g.V().hasLabel('Go').order().by('name').values('name')";
client.execute(query, (err, results) => {
	if (!err) {
		results.forEach(function (res){
			goTerms.push({
				name: res
			});
		});
		//console.log(cancerNames);
	}
});

//load genesymbols for autocomplete
var geneSymbols = [];
query = "g.V().hasLabel('Gene').order().by('nomenclatureAuthoritySymbol').values('nomenclatureAuthoritySymbol')";
client.execute(query, (err, results) => {
	if (!err) {
		results.forEach(function (res){
			geneSymbols.push({
				name: res
			});
		});
		//console.log(cancerNames);
	}
});

//load genesymbols for autocomplete
var proteinNames = [];
query = "g.V().hasLabel('Protein').order().by('name').values('name')";
client.execute(query, (err, results) => {
	if (!err) {
		results.forEach(function (res){
			proteinNames.push({
				name: res
			});
		});
		//console.log(cancerNames);
	}
});

app.get('/autocompleteProteinNames',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		console.log(req.query);
		if ("undefined"==typeof req.query) {
			// Handle errors 
			console.log('query is required');
			res.send('query is required');
		} else {
			var name = req.query.query;
			//console.log("term: " + name);
			
			var results = [];
			proteinNames.forEach(function (term){
				if (term.name.includes(name)) {
					results.push({
						label: term.name
					})
				}
			});
			
			//console.log(results);
			res.send(results);
		}
	}
);

app.get('/autocompleteGeneSymbols',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		//console.log(req.query);
		if ("undefined"==typeof req.query) {
			// Handle errors 
			console.log('query is required');
			res.send('query is required');
		} else {
			var symbol = req.query.query;
			//console.log("term: " + symbol);
			
			var results = [];
			geneSymbols.forEach(function (term){
				if (term.name.includes(symbol)) {
					results.push({
						label: term.name
					})
				}
			});
			
			//console.log(results);
			res.send(results);
		}
	}
);

app.get('/autocompleteGoTerms',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		//console.log(req.query);
		if ("undefined"==typeof req.query) {
			// Handle errors 
			console.log('query is required');
			res.send('query is required');
		} else {
			var goTerm = req.query.query;
			//console.log("term: " + goTerm);
			
			var results = [];
			goTerms.forEach(function (term){
				if (term.name.includes(goTerm)) {
					results.push({
						label: term.name
					})
				}
			});
			
			//console.log(results);
			res.send(results);
		}
	}
);

app.get('/autocompletePathwayNames',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		//console.log(req.query);
		if ("undefined"==typeof req.query) {
			// Handle errors 
			console.log('query is required');
			res.send('query is required');
		} else {
			var term = req.query.query;
			//console.log("term: " + term);
			
			var results = [];
			pathwayNames.forEach(function (pathway){
				if (pathway.name.includes(term)) {
					results.push({
						label: pathway.name
					})
				}
			});
			
			//console.log(results);
			res.send(results);
		}
	}
);

app.get('/autocompleteCancerNames',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		//console.log(req.query);
		if ("undefined"==typeof req.query) {
			// Handle errors 
			console.log('term is required');
			res.send('term is required');
		} else {
			var term = req.query.query;
			//console.log("term: " + term);
			
			var results = [];
			cancerNames.forEach(function (cancer){
				if (cancer.name.includes(term)) {
					results.push({
						label: cancer.name
					})
				}
			});
			
			//console.log(results);
			res.send(results);
		}
	}
);

app.get('/getProteinAccessions',
	function(req, res){
		var query = "g.V().hasLabel('Protein').has(id, vid).in('REFERS_TO').order().by(id,incr).values('name')";
		utils.executeQuery(req, res, client, query);
	}
);

app.get('/getPathwayProteins',
	function(req, res){
		var query = "g.V().hasLabel('Pathway').has(id, vid).out('CONTAINS').hasLabel('Protein').dedup().order().by(id,incr).valueMap('name','fullName')";
		utils.executeQuery(req, res, client, query);
	}
);

app.get('/getMatureValidatedTargets',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		if ("undefined"==typeof req.query.id) {
			// Handle errors 
			console.log('id is required');
			res.send('id is required');
		} else {
			var id = req.query.id;
			var query = "g.V().hasLabel('MiRNAmature').has(id, vid).in('INTERACTING_MIRNA').has('database','miRTarBase').as('interaction').out('INTERACTING_GENE').dedup().as('gene').select('gene','interaction')";
			//console.log("query: " + query);
			client.execute(query, { vid: id }, (err, results) => {
				var g = [];
				if (!err) {
					results.forEach(function (r){
						var symbol = r.gene.properties.nomenclatureAuthoritySymbol[0].value;
						var supportType = r.interaction.properties.supportType[0].value;
						var experiments = r.interaction.properties.experiments[0].value;
						experiments = experiments.replace('//', '; ');
						
						g.push({
							gene: symbol,
							supportType: supportType,
							experiments: experiments
						});
					});
					//console.log(g);
					res.send(g);
				} else {
					res.send(err);
				}
			});
		}
	}
);

app.get('/getMaturePredictedTargets',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		if ("undefined"==typeof req.query.id) {
			// Handle errors 
			console.log('id is required');
			res.send('id is required');
		} else {
			var id = req.query.id;
			var query = "g.V().hasLabel('MiRNAmature').has(id, vid).in('INTERACTING_MIRNA').has('database','miRanda').order().by('mirSvrScore').as('interaction').out('INTERACTING_GENE').as('gene').select('gene','interaction')";
			//console.log("query: " + query);
			client.execute(query, { vid: id }, (err, results) => {
				var g = [], p = [];
				if (!err) {
					results.forEach(function (r){
						var gene = r.gene.properties.nomenclatureAuthoritySymbol[0].value;
						var extTranscriptId = r.interaction.properties.extTranscriptId[0].value;
						var interactionId = r.interaction.id;
						var mirSvrScore = r.interaction.properties.mirSvrScore[0].value;
						
						if (gene != '-') {
							if ('undefined' == typeof p[gene]) {
								p[gene] = [];
							}
							p[gene].push({
								interactionId: interactionId,
								extTranscriptId: extTranscriptId,
								mirSvrScore: mirSvrScore
							});
						}
					});
					var keys = Object.keys(p);
					keys.forEach(function (k){
						g.push({
							gene: k,
							interactions: p[k]
						});
					});
					//console.log(g);
					res.send(g);
				} else {
					res.send(err);
				}
			});
		}
	}
);

app.get('/getMatures',
	function(req, res){
		var query = "g.V().hasLabel('MiRNA').has(id, vid).out('PRECURSOR_OF').dedup().order().by(id,incr).valueMap('accession','product')";
		utils.executeQuery(req, res, client, query);
	}
);

app.get('/getGene',
	function(req, res){
		var query = "g.V().hasLabel('Gene').has('nomenclatureAuthoritySymbol', vid)";
		utils.getNode(req, res, client, query);
	}
);

app.get('/getPathway',
	function(req, res){
		var query = "g.V().hasLabel('Pathway').has('pathwayId', vid)";
		utils.getNode(req, res, client, query);
	}
);

app.get('/getGo',
	function(req, res){
		var query = "g.V().hasLabel('Go').has('goId', vid)";
		utils.getNode(req, res, client, query);
	}
);

app.get('/getProtein',
	function(req, res){
		var query = "g.V().hasLabel('Protein').has('name', vid)";
		utils.getNode(req, res, client, query);
	}
);

app.get('/getCancer',
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		if ("undefined"==typeof req.query.id) {
			// Handle errors 
			console.log('id is required');
			res.send('id is required');
		} else {
			var id = req.query.id;
			var query = "g.V().hasLabel('Cancer').has(id, " + id + ").outE('CANCER2MIRNA').inV().dedup().path()";
			//console.log("query: " + query);
			client.execute(query, (err, results) => {
				if (!err) {
					var g = [];
					results.forEach(function (p){
						//console.log(p.objects[0].properties);
						var profile = p.objects[1].properties.profile;
						var mirnaId = p.objects[2].id;
						var mirnaName = p.objects[2].properties.name[0].value;
						//var mirnaDesc = p.objects[2].properties.description[0].value;
						g.push({
							id: mirnaId,
							name: mirnaName,
							//description: mirnaDesc,
							profile: profile
						});
					});
					var cancer = {
						wikipage: utils.wikipediaCancer[results[0].objects[0].properties.name[0].value],
						mirnas: g
					};
					//console.log(cancer);
					res.send(cancer);
				} else {
					res.send(err);
				}
			});
		}
	}
);

app.post('/query', 
	form( form.field("query").trim().required() ),
	function(req, res){
		res.setHeader("Access-Control-Allow-Origin", "*");
		if (!req.form.isValid) {
			// Handle errors 
			console.log(req.form.errors);
			res.send(req.form.errors);
		} else {
			var steps = req.form.query.split("."), newquery = "", isTraversal = false;
			var noPath = (steps[steps.length - 1]=='nopath');
			if (noPath) {
				steps.pop();
				newquery = "." + Array.prototype.join.call(steps, ".");
			} else {
				steps.forEach(function (step){
					var newstep;
					if(step.search(/out\(/)===0) {
						newstep = step.replace("out","outE");
						newquery += "." + newstep + ".inV()";
						isTraversal = true;
					} else
					if(step.search(/in\(/)===0) {
						newstep = step.replace("in","inE");
						newquery += "." + newstep + ".outV()"; 
						isTraversal = true;
	    	        } else {
	    	        	newquery += "." + step;
	    	        }
		    	});
				if (isTraversal) {
			    	newquery += ".path()";		
				}				
			}  
			newquery = newquery.substring(1);
		    //console.log("query:", newquery);
	     	client.execute(newquery, (err, results) => {
	     		if (!err) {
	     			//console.log(results);
	     			var g = [];
	     			results.forEach(function normalize(p){
	     				//console.log(p);
	     				var isRoot = true, parentNode;
	     				
	     				if("undefined"!=typeof p.label && "undefined"!=typeof p.properties) {
	     					var data = utils.mergeProperties(p);
	     					
	     					g.push({
	     						group: "nodes",
	     						data: data
	     					});
	     				} else
	     				if("undefined"!=typeof p.labels && "undefined"!=typeof p.objects){
	     					p.objects.forEach(function(el){
	     						if ("vertex"==el.type) {
	     							if (isRoot) {
	     								isRoot = false;
	     								parentNode = el.id;
	     							} else {
	     								el.parent = parentNode;
	     								parentNode = el.id;
	     							}
	     						}
		     					var group = ("vertex"==el.type) ? "nodes" : "edges";
		     					var data = utils.mergeProperties(el);
		     					
	     						g.push({
	     							group: group,
	     							//grabbable: false,
	     							data: data
	     						});
	     					});
	     				}
	     			});

     				//console.log(g);
				    res.send(g);
				} else {
					res.send(err);
				}
	     	});
		}
	}
);

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
