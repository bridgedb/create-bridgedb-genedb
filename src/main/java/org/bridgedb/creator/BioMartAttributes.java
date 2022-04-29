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

public class BioMartAttributes {
	private ArrayList<BioMartReference> array;

	public BioMartAttributes() {
		this.array = new ArrayList<BioMartReference>();
	}

	public void init() {
		try {
			InputStream is = BioMartAttributes.class.getClassLoader().getResourceAsStream("BioMartSources.tsv");
			if (is == null) System.out.println("[Error]: the BioMartSources.tsv is not present in src/main/resource/");
			loadAnInputStream(is);
		} catch (IOException ex) {
			throw new Error(ex);
		}
	}

	protected void loadAnInputStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] fields = line.split("\\t");
			BioMartReference ref = new BioMartReference(fields[0], fields[1], fields[2]);
			array.add(ref);
		}
	}

	public void addReference(BioMartReference ref) {
		array.add(ref);
	}

	public ArrayList<BioMartReference> getReference(String name) {
		ArrayList<BioMartReference> match = new ArrayList<BioMartReference>();
		for (BioMartReference ref : array) {
			if (ref.getGpmlName().equals(name) || ref.getIdName().equals(name) || ref.getQueryName().equals(name)) {
				match.add(ref);
			}
		}
		return match;
	}

	public BioMartAttributes(ArrayList<BioMartReference> array) {
		this.array = array;
	}

	public ArrayList<BioMartReference> getArray() {
		return array;
	}

	public void setArray(ArrayList<BioMartReference> array) {
		this.array = array;
	}

	@Override
	public String toString() {
		return "BioMartAttributes [array=" + array + "]";
	}
}
