# BridgeDb database building for gene databases

Introduction
============
A script to create a gene-focussed BrigdeDb database based on Ensembl BioMART.

Installation
============

Java 11 is required.

Compile the code with:

```shell
mvn clean install
cp target/org.bridgedb.genedb-jar-with-dependencies.jar BioMart2BridgeDb.jar
``` 

Run
============
In your terminal:

```shell
java -jar BioMart2BridgeDb.jar <configFile> <outputPath> <oldDB> <inclusive>
```

- \<configFile\>: location of configuration file

- \<outputPath\>: Path for the new database

- \<oldDB\>: (optional) directory of the old database - run QC

- \<inclusive\>: (optional) use inclusive BridgeDb list

List of default config files:
============

Configuration files can be found in https://github.com/bridgedb/create-bridgedb-genedb-config/tree/master/resource .

Example: [Bos taurus config file](https://raw.githubusercontent.com/bridgedb/create-bridgedb-genedb-config/master/resource/BosTaurus.config)

How to create your own config file
============
* Give the version of Ensembl BioMart to query:

    e.g: http://www.ensembl.org/biomart/, http://oct2014.archive.ensembl.org/biomart/,	http://metazoa.ensembl.org/biomart/

    **endpoint**=http://plants.ensembl.org/biomart/
    
    You can find an overview of releases in the [Ensembl Archive](http://www.ensembl.org/info/website/archives/index.html).

* MartRegistry can be found there:
    
    http://plants.ensembl.org/biomart/martservice?type=registry

    e.g: protists_mart_27, metazoa_mart_27, default

    **schema**=plants_mart_27

* Code name of the species: http://www.ensembl.org/biomart/martservice?type=datasets&mart=ENSEMBL_MART_ENSEMBL

    **species**=athaliana_eg_gene

* The name of the bridge database

    **database_name**=Arabidopsis thaliana genes and proteins

* The name of the file .bridge created

    **file_name**=At_Derby_Ensembl_Plant_28

* The different data source code name can be found like there:

    http://plants.ensembl.org/biomart/martservice?type=attributes&mart=plants_mart_27&dataset=athaliana_eg_gene

    **probe_set**=affy_ath1_121501
    **gene_datasource**=refseq_mrna,refseq_ncrna,refseq_peptide,uniprot_sptrembl,pdb,tair_locus,go_accession,unigene,entrezgene,wikigene_id,nasc_gene_id,uniprot_swissprot_accession

* Optional filters (chromosome list)

    e.g: **chromosome_name**=1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,X,MT
 
    http://may2015.archive.ensembl.org/biomart/martservice?type=filters&dataset=btaurus_gene_ensembl
