[![Java CI with Maven](https://github.com/bridgedb/create-bridgedb-genedb/actions/workflows/maven.yml/badge.svg)](https://github.com/bridgedb/create-bridgedb-genedb/actions/workflows/maven.yml)
# BridgeDb database building for gene databases

Introduction
============
A script to create a gene-focussed BrigdeDb database based on Ensembl BioMART.

Installation
============

Java 8 is required for PathVisio 3.x support.

Compile the code with:

```shell
mvn clean install
cp target/org.bridgedb.genedb-jar-with-dependencies.jar BioMart2BridgeDb.jar
``` 

Run
============
In your terminal:

```shell
java -jar BioMart2BridgeDb.jar $DATASOURCENAME $VERSION $CONFIG_FILE $PATH_FOR_NEW_DB
```
- \<DATASOURCENAME\>: Database name (e.g. Ensembl, EnsemblGenomes)
- \<VERSION\>: Database version (e.g. 111)
- \<CONFIG_FILE\>: configuration file
- \<PATH_FOR_NEW_DB\>: Path for the new database

List of default config files:
============

Configuration files can be found in https://github.com/bridgedb/create-bridgedb-genedb-config/tree/master/configFiles.

Example: [Arabidopsis thaliana config file](https://github.com/bridgedb/create-bridgedb-genedb-config/blob/master/configFiles/plants/v49/At-v49.config)

How to create your own config file
============
* Give the version of Ensembl BioMart to query:

    e.g: http://www.ensembl.org/biomart/, http://oct2014.archive.ensembl.org/biomart/,	http://nov2020-metazoa.ensembl.org/biomart/

    **endpoint**=https://nov2020-plants.ensembl.org/biomart/
    
    You can find an overview of releases in the [Ensembl Archive](http://www.ensembl.org/info/website/archives/index.html), [Metazoa Archive](https://metazoa.ensembl.org/index.html), [Plants Archive](https://plants.ensembl.org/index.html), [Fungi Archive](https://fungi.ensembl.org/index.html).

* MartRegistry for plants v49 can be found here:
    
    https://nov2020-plants.ensembl.org/biomart/martservice?type=registry

    e.g: plants_mart, metazoa_mart, default

    **schema**=plants_mart

* Code name of the animal species: http://www.ensembl.org/biomart/martservice?type=datasets&mart=ENSEMBL_MART_ENSEMBL, [Metazoa v49](https://nov2020-metazoa.ensembl.org/biomart/martservice?type=datasets&mart=metazoa_mart), [Plants v49](https://nov2020-plants.ensembl.org/biomart/martservice?type=datasets&mart=plants_mart) and, [Fungi v49](https://nov2020-fungi.ensembl.org/biomart/martservice?type=datasets&mart=fungi_mart)

    **species**=athaliana_eg_gene

* The name of the bridge database

    **database_name**=Arabidopsis thaliana genes and proteins

* The name of the file .bridge created

    **file_name**=At_Derby_Ensembl_Plant_49

* The different data source code name for *Arabidopsis thaliana* can be found here:

    https://nov2020-plants.ensembl.org/biomart/martservice?type=attributes&mart=plants_mart&dataset=athaliana_eg_gene

    **probe_datasource**=Affy,Agilent
	**probe_set**=affy_aragene,affy_ath1_121501,agilent_g2519f_015059,agilent_g2519f_021169,agilent_g4136a_011839,agilent_g4136b_013324,agilent_g4142a_012600
	**gene_datasource**=entrezgene_id,go_id,mirbase_accession,mirbase_id,pdb,refseq_dna,refseq_peptide,uniprotsptrembl,uniprotswissprot,tair_locus,nasc_gene_id
	
	
* Optional filters (chromosome list) for *Arabidopsis thaliana* can be found here:
	https://nov2020-plants.ensembl.org/biomart/martservice?type=filters&dataset=athaliana_eg_gene

    e.g: **chromosome_name**=1,2,3,4,5,Pt,Mt
 
    
