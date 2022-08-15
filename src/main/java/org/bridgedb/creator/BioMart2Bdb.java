// Creating BridgeDb Ensembl Gene Databases
// Copyright 2012-2021 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.creator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.w3c.dom.Document;

public class BioMart2Bdb {

	private BioMartAttributes bio;
	private HashMap<Xref, HashSet<Xref>> dbEntries = new HashMap<Xref, HashSet<Xref>>();
	private HashMap<Xref, GeneAttributes> geneSet = new HashMap<Xref, GeneAttributes>();
	private SpeciesConfiguration config;
	private Logger logger = Logger.getLogger("Bdb_creation");

	public BioMart2Bdb(SpeciesConfiguration config, BioMartAttributes bio, HashMap<Xref, HashSet<Xref>> dbEntries,
			HashMap<Xref, GeneAttributes> geneSet) {
		this.config = config;
		this.bio = bio;
		this.dbEntries = dbEntries;
		this.geneSet = geneSet;
	}

	public void query(String externalSource, boolean attributesFilter, boolean chromosomeFilter) {
		Document result = QueryBioMart.createQuery(config, externalSource, attributesFilter, chromosomeFilter);
		InputStream is = QueryBioMart.getDataStream(result, config.getEndpoint());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			parse(br, externalSource);
		} catch (IOException e) {
			// TODO: throw exception
			e.printStackTrace();
		}
	}

	public void parse(BufferedReader br, String externalSource) throws IOException {
		String line = br.readLine();
		String[] split = line.split("\t");
		DataSource ds = null;

		try {
			// unify all UniProt BioMart property names to UniProt TrEMBL
			if (externalSource.equals("uniprot_swissprot_accession") || externalSource.equals("uniprot_swissprot")
					|| externalSource.equals("uniprotswissprot") || externalSource.equals("uniprotsptrembl")) {
				externalSource = "uniprot_sptrembl";
			}

			System.out.println("Downloading:" + split[1]);
			ArrayList<BioMartReference> bioMartReference = bio.getReference(externalSource);

			if (bioMartReference.size() == 0) {
				System.err.println(externalSource + " not present in BioMartSources file (BioMartSources.tsv)");
			} else {
				String gpmlName = bioMartReference.get(0).getGpmlName();
				ds = DataSource.getExistingByFullName(gpmlName);

				line = br.readLine(); // Skip the header
				while (line != null) {
					split = line.split("\t");
					Xref mainXref = new Xref(split[0], DataSource.getExistingBySystemCode("En"));

					// only parse if there is a external reference in this Ensembl id
					if (split[1].length() > 1) {
						Xref xref = new Xref(split[1], ds);
						GeneAttributes gene = new GeneAttributes(split[2], split[3], split[4], split[5]);
						geneSet.put(mainXref, gene);
						geneSet.put(xref, gene);
						HashSet<Xref> xrefSet = dbEntries.get(mainXref);
						if (xrefSet == null) {
							HashSet<Xref> database = new HashSet<Xref>();
							database.add(xref);
							dbEntries.put(mainXref, database);
						} else {
							xrefSet.add(xref);
						}
					} else {
						if (!dbEntries.containsKey(mainXref)) {
							HashSet<Xref> database = new HashSet<Xref>();
							dbEntries.put(mainXref, database);
						}
					}
					line = br.readLine();
				}
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			logger.info("Incorrect datasource: " + split[0] + "\n" + "Please check the datasource here: "
					+ config.getEndpoint() + "martservice?type=attributes&dataset=" + config.getSpecies());
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			logger.info(externalSource + " is not present in the BioMartSources.tsv file,"
					+ " please add it to conver into a BridgeDb datasource" + "\n"
					+ "BridgeDb datasource: https://raw.githubusercontent.com/bridgedb/BridgeDb/master/org.bridgedb.bio/resources/org/bridgedb/bio/datasources.txt"
					+ "\n" + "BioMart datasource: " + config.getEndpoint() + "martservice?type=attributes&dataset="
					+ config.getSpecies());
		} catch (Exception e) {
			e.printStackTrace();
		}
		br.close();
	}

	public void bridgedbCreator(Map<Xref, HashSet<Xref>> dbEntries, Map<Xref, GeneAttributes> geneSet, String path, String datasourceName, String version)
			throws IDMapperException, ClassNotFoundException {

		BridgeDbCreator creator = new BridgeDbCreator(dbEntries);
		creator.setOutputFilePath(path + config.getFileName());
		creator.setDbSourceName(datasourceName);
		creator.setDbVersion(version);
		creator.setDbSeries(config.getDBName());
		creator.setDbDataType("GeneProduct");

		DbBuilder dbBuilder = new DbBuilder(creator);
		dbBuilder.createNewDb();
		dbBuilder.addEntry(dbEntries, geneSet);
		dbBuilder.finalizeDb();

		System.out.println(dbBuilder.getError() + " errors (duplicates) occurred" + dbBuilder.getErrorString());
	}

	public HashMap<Xref, HashSet<Xref>> getDbEntries() {
		return dbEntries;
	}

	public void setDbEntries(HashMap<Xref, HashSet<Xref>> dbEntries) {
		this.dbEntries = dbEntries;
	}

	public BioMartAttributes getBio() {
		return bio;
	}

	public void setBio(BioMartAttributes bio) {
		this.bio = bio;
	}

	public HashMap<Xref, GeneAttributes> getGeneSet() {
		return geneSet;
	}

	public void setGeneSet(HashMap<Xref, GeneAttributes> geneSet) {
		this.geneSet = geneSet;
	}
}
