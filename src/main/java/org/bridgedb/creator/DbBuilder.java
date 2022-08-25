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
package org.bridgedb.creator;// a small wizard for easily creating BridgeDb Derby databases

// Copyright 2012 BiGCaT Bioinformatics
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.GdbConstruct;
import org.bridgedb.rdb.construct.GdbConstructImpl4;

/**
 * The class that performs the actual work on the database, used by
 * {@link ProgressPage}.
 * 
 * @author Stefan
 */
public class DbBuilder {

	private GdbConstruct newDb;
	private BridgeDbCreator creator;
	private int olderror;
	private int progress;
	private HashSet<Xref> addedXrefs;
	private String errorString;
	private int error;

	public DbBuilder(BridgeDbCreator creator) {
		this.creator = creator;
	}

	/**
	 * Creates an empty Derby database using the output file selected in the
	 * {@link FilePage} and the description from the {@link DescriptionPage}.
	 * 
	 * @author Stefan
	 * @throws IDMapperException when it cannot write to the output file
	 */
	public void createNewDb() throws IDMapperException {
		newDb = new GdbConstructImpl4(creator.getOutputFilePath(), new DataDerby(), DBConnector.PROP_RECREATE);
		newDb.createGdbTables();
		newDb.preInsert();

		error = 0;
		olderror = 0;
		progress = 0;
		errorString = new String("");
		addedXrefs = new HashSet<Xref>();

		String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
		newDb.setInfo("BUILDDATE", dateStr);
		newDb.setInfo("DATASOURCENAME", creator.getDbSourceName());
		newDb.setInfo("DATASOURCEVERSION", creator.getDbVersion());
		newDb.setInfo("SERIES", creator.getDbSeries());
		newDb.setInfo("DATATYPE", creator.getDbDataType());
	}

	/**
	 * Adds mapping entries to the Derby database created by {@link createNewDb}.
	 * 
	 * @param dbEntries a {@link Map} containing a keyset of {@link Xref}s and
	 *                  values that are {@link List}s of {@link Xref}s.
	 * @throws IDMapperException
	 */
	public void addEntry(Map<Xref, HashSet<Xref>> dbEntries, Map<Xref, GeneAttributes> geneSet)
			throws IDMapperException {
		int i = 0;
		for (Xref ref : dbEntries.keySet()) {
			progress++;
			Xref mainXref = ref;
			if (addedXrefs.add(mainXref))
				if (error > olderror)
					errorString = (errorString + error + "\t add gene " + mainXref.getId() + " "
							+ mainXref.getDataSource().getFullName() + "\n");
			error += newDb.addGene(mainXref);

			if (!addedXrefs.contains(mainXref)) {
				error += newDb.addGene(mainXref);
				if (error > olderror)
					errorString = (errorString + error + "\t add gene " + mainXref.getId() + " "
							+ mainXref.getDataSource().getFullName() + "\n");
				olderror = error;
				addedXrefs.add(mainXref);
			}
			error += newDb.addLink(mainXref, mainXref);
			if (error > olderror)
				errorString = (errorString + error + "\t add link between " + mainXref.getId() + " "
						+ mainXref.getDataSource().getFullName() + " and " + mainXref.getId() + " "
						+ mainXref.getDataSource().getFullName() + "\n");
			olderror = error;

			GeneAttributes gene = geneSet.get(mainXref);
			if (gene != null && gene.getSymbol() != null)
				error += newDb.addAttribute(mainXref, "Symbol", gene.getSymbol());
			if (gene != null && gene.getType() != null)
				error += newDb.addAttribute(mainXref, "Type", gene.getType());
			if (gene != null && gene.getDescription() != null)
				error += newDb.addAttribute(mainXref, "Description", gene.getDescription());
			if (gene != null && gene.getChromosme() != null)
				error += newDb.addAttribute(mainXref, "Chromosome", gene.getChromosme());

			for (Xref rightXref : dbEntries.get(mainXref)) {
				if (!rightXref.equals(mainXref) && rightXref != null) {
					if (addedXrefs.add(rightXref))
						error += newDb.addGene(rightXref);
					if (error > olderror)
						errorString = (errorString + "  " + error + "\t add gene " + rightXref.getId() + " "
								+ rightXref.getDataSource().getFullName() + "\n");

					gene = geneSet.get(rightXref);
					String systemCode = rightXref.getDataSource().getSystemCode();
					if ((systemCode.equals("L") == true) || (systemCode.equals("H") == true)
							|| (systemCode.equals("S") == true)) {
						if (gene != null && gene.getSymbol() != null)
							error += newDb.addAttribute(rightXref, "Symbol", gene.getSymbol());
						if (gene != null && gene.getType() != null)
							error += newDb.addAttribute(rightXref, "Type", gene.getType());
						if (gene != null && gene.getDescription() != null)
							error += newDb.addAttribute(rightXref, "Description", gene.getDescription());
						if (gene != null && gene.getChromosme() != null)
							error += newDb.addAttribute(rightXref, "Chromosome", gene.getChromosme());
					}
					if (!addedXrefs.contains(rightXref)) {
						error += newDb.addGene(rightXref);
						if (error > olderror)
							errorString = (errorString + "  " + error + "\t add gene " + rightXref.getId() + " "
									+ rightXref.getDataSource().getFullName() + "\n");
						olderror = error;
						addedXrefs.add(rightXref);
					}
					error += newDb.addLink(mainXref, rightXref);
					if (error > olderror)
						errorString = (errorString + "  " + error + "\t add link between " + mainXref.getId() + " "
								+ mainXref.getDataSource().getFullName() + " and " + rightXref.getId() + " "
								+ rightXref.getDataSource().getFullName() + "\n");
					olderror = error;
				}
			}
			olderror = error;
			i++;
			if (i % 1000 == 0)
				System.out.println(i);
		}
	}

	public void addEntry(Map<Xref, HashSet<Xref>> dbEntries, Map<Xref, SnpAttributes> geneSet, boolean flag)
			throws IDMapperException {
		int i = 0;
		for (Xref mainXref : dbEntries.keySet()) {
			progress++;
			if (addedXrefs.add(mainXref))
				newDb.addGene(mainXref);
			newDb.addLink(mainXref, mainXref);

			SnpAttributes gene = geneSet.get(mainXref);
			if (gene != null && gene.getSymbol() != null)
				newDb.addAttribute(mainXref, "Symbol", gene.getSymbol());
			if (gene != null && gene.getChrom_start() != null)
				newDb.addAttribute(mainXref, "Chromosome position start (bp)", gene.getChrom_start());
			if (gene != null && gene.getChrom_end() != null)
				newDb.addAttribute(mainXref, "Chromosome position end (bp)", gene.getChrom_end());
			if (gene != null && gene.getChromosme() != null)
				newDb.addAttribute(mainXref, "Chromosome", gene.getChromosme());
			if (gene != null && gene.getAllele() != null)
				newDb.addAttribute(mainXref, "Variant Alleles", gene.getAllele());
			if (gene != null && gene.getMinor_allele_freq() != null)
				newDb.addAttribute(mainXref, "MAF", gene.getMinor_allele_freq());

			for (Xref rightXref : dbEntries.get(mainXref)) {
				if (!rightXref.equals(mainXref) && rightXref != null) {
					if (addedXrefs.add(rightXref))
						newDb.addGene(rightXref);
					newDb.addLink(mainXref, rightXref);
				}
			}
			i++;
			if (i % 1000 == 0) {
				System.out.println(i);

				newDb.commit();
			}
		}
	}

	public void finalizeDb() throws IDMapperException {
		newDb.finalize();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}
}
