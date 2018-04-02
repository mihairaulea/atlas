# Atlas

The world is a graph. Here's the graph technology that allows one to extract, transform and load it. Also, methods to query and analyse it. Batteries included.

This software aims to make it simple to parse, import and analyze Geo data, and apply various network analytics, such as road topology and city connectivity. It uses geo hashing to index geo points.

Work in progress.

# Install

Clone this repo, and run gradle build. 

# Features

* Gis data abstractions. Core methods to parse, filter, transform and write to Neo4j database.
* Clearly defined data for OSM entities
* Parsing and filtering of OSM data based on Java Stream API
* Importing OpenStreetMap data
* CRUD Neo4j methods for Node, Way and Relation
* Geohash indexing
* Database transformers: contract the graph, link interesting entities with easy predicates, remove unneccessary entities, summarize information
* GIS operations
* Network analysis tools
* Importing 3rd party geo data(weather, population data, real-time events, wikipedia data, transport data and much more)

# Core 

Easily configure the way you want Atlas to work. What's the label on certain node type? What happens to properties of core nodes? What happens to tags? Easy methods to parse the data file, and generate a Stream out of it, in a memory and computational efficient way. Parse > Transform > Write. Query. Transform and analyse. What type of entities get indexed using the geo hash?

# Parsing OSM data

Atlas allows easy loading, transforming and filtering of OpenStreetMap data, using Java Streams API. You decide what happens with the loaded entities. Do you write as soon as you load an entity? Do you just process them in memory, and write a summary to the database? 

Some questions you can answer pretty fast with Atlas. Do you want to know how many residential building are in an area? What's the total area of administrative buildings in London? What's the percentage occupied by townhall buildings in Europe's capitals?  

~~~java
IAtlasParser parser = new OSMParser();
// count the number of trees in London
parser.getEntityStream("london.osm")
.filter(osmEntity -> osmEntity instanceof FastNode)
.map(osmEntity -> (FastNode)osmEntity)
.filter(fastNode -> fastNode.getTags().contains("natural**tree"))
.count();
~~~

# Importing OSM data

OpenStreetMap operates with 3 basic concepts. Nodes, Ways and Relations. Atlas exposes configurable methods to write such entities to Neo4j.

~~~java
IAtlasParser parser = new OSMParser();
FastWriter writer = new FastWriter();
AtlasIndex index = new AtlasIndex();
// let's get all the non-residential buildings, insert and index them into the neo4j database
parser.getEntityStream("data-source.osm")
.filter(osmEntity -> osmEntity instanceof FastNode)
.map(osmEntity -> (FastNode)osmEntity)
.filter(fastNode -> fastNode.getTags().stream().allMatch(tag -> tag.startsWith("building")))
.filter(fastNode -> fastNode.getTags().stream().noneMatch(tag -> tag.equals("building**residential")))
.map(fastNode -> writer.addNode(fastNode));
~~~

# Neo4j CRUD operations
Creating an OSM node, Way or Relation becomes easy.
~~~java
FastWriter writer = new FastWriter();
writer.start();
FastNode fastNode = new FastNode();
// ...
writer.addNode(fastNode);
Way way = new Way();
// ...
writer.addWay(way);
Relation relation = new Relation();
//
writer.addRelation(relation);
~~~

Using Cypher, it becomes easy to read the database and have an answer to complex questions.

~~~java
MATCH (n:ResidentialBuilding) return COUNT(n);
~~~

Updating the database can also be done via cypher.

MATCH (n:ResidentialBuilding) set(n.markForRemoval='1') return COUNT(n);

Deleting is also fairly simple.

MATCH (n:ResidentialBuilding {markForRemoval:1}) DETACH DELETE(n);

### Geohash indexing

~~~java
IAtlasParser parser = new OSMParser();
FastWriter writer = new FastWriter();
AtlasIndex index = new AtlasIndex();

// let's just add everything in the osm file to the database, and index it
parser.getEntityStream("data-source.osm")
      .map(osmEntity -> writer.addOSMEntity(osmEntity))
      .map(node -> index.addToIndex(node));
~~~

### Database transformers

Let's say you want to obtain a simplified graph of Europe, comprised of big towns and the highways that connect them. After a full import of Europe.osm:

~~~java
RemoveCriteria removeIfNotTownOrHighwayTagsAndNotInsideTown = new RemoveCriteria();
writer.removeNodes(removeIfNotTownOrHighwayTagsAndNotInsideTown);

RemoveCriteria removeIfInsideTownBoundary = new RemoveCriteria();
RemoveSummary insideTownRemoveSummary = writer.removeNodes(removeIfInsideTownBoundary);
writer.updateWithRemoveSummary(insideTownRemoveSummary);

LinkCriteria linkIfClose = new LinkCriteria();
atlasTransformer.applyLinking(db, linkIfClose);

RemoveCriteria removeIfDisconnectedNode = new RemoveCriteria();
writer.removeNodes(removeIfDisconnectedNode);

ContractCriteria contractIfArea = new ContractCriteria();
atlasTransformer.applyContraction(db, contractIfArea);

ContractCriteria contractIfWayPointWithNoPOINearby = new ContractCriteria();
atlasTransformer.applyContraction(db, contractIfWayPointWithNoPOINearby);

RemoveCriteria removeIfWayNotConnected = new RemoveCriteria();
writer.removeNodes(removeIfWayNotConnected);
~~~

### GIS operations

Given a list of coordinates, one can extract points inside. Integration with Java Gis library pending. Aim to support:

* Contain
* Cover
* Covered By
* Cross
* Disjoint
* Intersect
* Intersect Window
* Overlap
* Touch
* Within
* Within Distance

### Network analysis tools

Once you specify a subgraph, Atlas can detect cliques, determin a graph's coloring, wether or not the subgraph is connected, inpect it's biconnectivity, the strongly connected components, the max flow from one node to another, compute it's betweeness centrality(as well as coreness, harmonic centrality and page rank), solve the travelling salesman's probleme, and much more!

Stay tunned for a series of blog posts about how we used this and what insights we uncovered!

### Importing 3rd party data

Various datasets have been imported over the current Atlas structure, to answer complex questions such as: Is there a mountain town, with a GDP per capita over 50k, where the weather is always between 14C and 28C?  
