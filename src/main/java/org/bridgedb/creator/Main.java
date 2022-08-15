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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.DataSourceTxt;
import org.bridgedb.tools.qc.BridgeQC;

public class Main {
	private static boolean qc;
	private static boolean inclusive;
	private static boolean chrGiven;
	protected static Logger logger;

	private static void printUsage() {
		System.out.println("Usage: java -jar BioMart2BridgeDb.jar <configurationFile> <newDatabase> "
				+ "(optional)<QC>  (optional)<inclusive>");
		System.out.println("<configurationFile> location of configuration file");
		System.out.println("<newDatabase> location for the new database");
		System.out.println("<QC> (optional) directory of the old database - run QC");
		System.out.println("<inclusive> (optional) use inclusive BridgeDb list");
	}

	/**
	 * java -jar BioMart2BridgeDb.jar arg1 arg2 arg3 arg4 arg1: location of
	 * configuration file arg2: location for the new database arg3: (optional)
	 * directory of the old database - run QC arg4: (optional) use inclusive
	 * BridgeDb list
	 *
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IDMapperException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ClassNotFoundException, IDMapperException, IOException {

		logInit();
		DataSourceTxt.init(); // Initialize BrideDb data source
		File dir = null;
		String datasourceName = null;
		String version = null;
		String path = null;
		String pathOld = null;
//		Boolean qc = false;
//		Boolean inclusive = false;

		switch (args.length) {
		case 0:
			printUsage();
			break;
		case 1:
			printUsage();
			break;
		case 2:
			printUsage();
			break;
		case 3:
			printUsage();
			break;
		case 4:
			datasourceName = args[0];
			version = args[1];
			dir = new File(args[2]);
			path = args[3];
			if (dir.isDirectory()) {
				FilenameFilter textFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						String lowercaseName = name.toLowerCase();
						if (lowercaseName.endsWith(".config")) {
							return true;
						} else {
							return false;
						}
					}
				};
				File[] listFiles = dir.listFiles(textFilter);
				for (File f : listFiles) {
					SpeciesConfiguration config = new SpeciesConfiguration(f.getAbsolutePath());
					runDB(config, path, inclusive, datasourceName, version);
					if (qc)
						report(false, pathOld, path, config);
				}
			} else {
				SpeciesConfiguration config = new SpeciesConfiguration(dir.getAbsolutePath());

				runDB(config, path, inclusive, datasourceName, version);
				if (qc)
					report(false, pathOld, path, config);
			}
			break;
		case 5:
			datasourceName = args[0];
			version = args[1];
			dir = new File(args[2]);
			path = args[3];
			pathOld = args[4];
			qc = true;
			if (dir.isDirectory()) {
				FilenameFilter textFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						String lowercaseName = name.toLowerCase();
						if (lowercaseName.endsWith(".config")) {
							return true;
						} else {
							return false;
						}
					}
				};
				File[] listFiles = dir.listFiles(textFilter);
				for (File f : listFiles) {
					SpeciesConfiguration config = new SpeciesConfiguration(f.getAbsolutePath());
					runDB(config, path, inclusive, datasourceName, version);
					if (qc)
						report(false, pathOld, path, config);
				}
			} else {
				SpeciesConfiguration config = new SpeciesConfiguration(dir.getAbsolutePath());
				runDB(config, path, inclusive, datasourceName, version);
				if (qc)
					report(false, pathOld, path, config);
			}
			break;
		}
	}

	/**
	 * Run the creation of the new database
	 *
	 * @param config         Configuration file for this species
	 * @param path           A pathname string for the new generated database
	 * @param inducle_Filter If true, select only the datasources form the BridgeDb
	 *                       inclusive list (see
	 *                       {@link script.SpeciesConfiguration})
	 * @throws ClassNotFoundException
	 * @throws IDMapperException
	 */
	public static void runDB(SpeciesConfiguration config, String path, Boolean inducle_Filter, String datasourceName, String version)
			throws ClassNotFoundException, IDMapperException {

		Date date = new Date();
		System.out.println(date);

		HashMap<Xref, HashSet<Xref>> dbEntries = new HashMap<Xref, HashSet<Xref>>(); // Contains the mapping from
																						// Ensembl to external database
		HashMap<Xref, GeneAttributes> geneSet = new HashMap<Xref, GeneAttributes>(); // Contains the gene attributes of
																						// the Ensembl gene id

		BioMartAttributes bio = new BioMartAttributes();
		// Initialize the loading of the default mapping between BioMart and BridgeDb
		bio.init();
		// Query the BioMart attributes for probes ids.
		QueryBioMart.loadBiomartAttributes(bio, config);
		BioMart2Bdb mart = new BioMart2Bdb(config, bio, dbEntries, geneSet);

		// Checking if the chromosome names are given
		chrGiven = !config.getChromosome().isEmpty();
		if (!chrGiven) System.out.println("[INFO]: the chromosome names are not given.");

		System.out.println("Start to download each datasources");
		// Probe queries
		try {
			for (String probe : config.getProbeSet()) {
				mart.query(probe, true, chrGiven);
			}
		} catch (NullPointerException ne) {
			for (String probe : config.getProbe()) {
				for (BioMartReference ref : bio.getReference(probe)) {
					mart.query(ref.getQueryName(), true, chrGiven);
				}
			}
		}
		// Genes & proteins queries
		List<String> datasources;
		if (inducle_Filter)
			datasources = config.filterDatasource(config.getDatasource(), bio);
		else
			datasources = config.getDatasource();
		for (String ds : datasources) {
			mart.query(ds, true, chrGiven);
		}
		System.out.println(date);
		System.out.println("Start to the creation of the database, might take some time");
		mart.bridgedbCreator(dbEntries, geneSet, path, DATASOURCENAME, VERSION);
		System.out.println(date);
	}

	/**
	 * Run the creation of the QC & WP reports of the new database
	 *
	 * @param allSpecies If true create the report with all the outdated ids for all
	 *                   the species of Wikipathways
	 * @param pathOld    A pathname string for the previous databases
	 * @param path       A pathname string for the of new generated database
	 * @param config     Configuration file for this species
	 */
	public static void report(Boolean allSpecies, String pathOld, String path, SpeciesConfiguration config) {

		File dir = new File(pathOld);
		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".bridge")) {
					return true;
				} else {
					return false;
				}
			}
		};
		File[] listFiles = dir.listFiles(textFilter);
		String symbol = "" + config.getFileName().toUpperCase().charAt(0) + config.getFileName().charAt(1);
		File oldDB = null;
		for (File f : listFiles) {
			if (f.getName().startsWith(symbol)) {
				oldDB = f;
			}
		}
		try {
			runQC(oldDB, path, config.getFileName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run the Quality Control between the previous database and the new generated
	 * database.<br>
	 * Create the .qc file at the location provided
	 *
	 * @param oldDB  File - location - of the previous database
	 * @param path   Path of new generated database
	 * @param dbName Filename of the new generated database
	 */
	public static void runQC(File oldDB, String path, String dbName) {
		try {
			String fileName = path + "report_" + dbName + ".qc";
			OutputStream out = new FileOutputStream(fileName);
			BridgeQC main = new BridgeQC(oldDB, new File(path + dbName + ".bridge"), out);
			main.run();
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Please check the path of the old database");
		}

	}

	/**
	 * Initialization of the log file
	 */
	public static void logInit() {
		logger = Logger.getLogger("Bdb_creation");
		FileHandler fh;
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler("Bdb_creation.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
