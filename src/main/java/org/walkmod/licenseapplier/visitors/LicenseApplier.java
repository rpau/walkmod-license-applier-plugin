/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/

package org.walkmod.licenseapplier.visitors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.walkmod.exceptions.WalkModException;
import org.walkmod.javalang.ast.BlockComment;
import org.walkmod.javalang.ast.Comment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class LicenseApplier extends VoidVisitorAdapter<VisitorContext> {

	/**
	 * File with the license text
	 */
	private File licenseFile;

	/**
	 * Different words without blank characters
	 */
	private String[] licenseWords;

	/**
	 * license content with resolved variables
	 */
	private String licenseContent;

	/**
	 * property values to replace in the license
	 */
	private Map<String, String> propertyValues = new HashMap<String, String>();

	private String action = REFORMAT_ACTION;

	/**
	 * update existing header with a new one
	 */
	public static final String UPDATE_ACTION = "update";

	/**
	 * check if header is missing in some source file
	 */
	public static final String CHECK_ACTION = "check";

	/**
	 * can remove existing header
	 */
	public static final String REMOVE_ACTION = "remove";

	/**
	 * add headers if missing
	 */
	public static final String REFORMAT_ACTION = "reformat";

	public void setAction(String action) {
		if (action != null) {
			if (UPDATE_ACTION.equalsIgnoreCase(action)) {
				this.action = UPDATE_ACTION;
			} else if (CHECK_ACTION.equalsIgnoreCase(action)) {
				this.action = CHECK_ACTION;
			} else if (REMOVE_ACTION.equalsIgnoreCase(action)) {
				this.action = REMOVE_ACTION;
			} else if (REFORMAT_ACTION.equalsIgnoreCase(action)) {
				this.action = REFORMAT_ACTION;
			}
		}
	}

	private boolean isVariable(String word) {
		return word.startsWith("${") && word.endsWith("}");
	}

	private boolean matchesWithLicense(String content) {
		int matchingIndex = 0;
		boolean containsLicense = false;
		if (content != null) {
			String[] words = content.split("\\s");
			if (words != null) {
				for (int i = 0; i < words.length && !containsLicense; i++) {
					String word = words[i].trim();
					if (!"".equals(word)) {
						if (isVariable(licenseWords[matchingIndex])
								|| word.equals(licenseWords[matchingIndex])) {
							matchingIndex++;
						} else {
							matchingIndex = 0;
						}
						containsLicense = matchingIndex == licenseWords.length - 1;
					}
				}
			}
		}
		return containsLicense;
	}

	@Override
	public void visit(CompilationUnit cu, VisitorContext ctx) {
		if (licenseFile == null) {
			throw new WalkModException("Missing license file");
		}
		boolean licenseFound = false;
		List<Comment> comments = cu.getComments();
		Comment comment = null;
		boolean referenceIsPackage = false;
		Node reference = cu.getPackage();
		if (reference == null) {
			if (cu.getImports() != null && !cu.getImports().isEmpty()) {
				reference = cu.getImports().get(0);
			} else if (cu.getTypes() != null && !cu.getTypes().isEmpty()) {
				reference = cu.getTypes().get(0);
			}
		} else {
			referenceIsPackage = true;
		}
		if (comments != null) {
			Iterator<Comment> it = comments.iterator();
			while (it.hasNext() && !licenseFound) {
				comment = it.next();
				if (reference == null
						|| comment.isPreviousThan(reference)) {
					licenseFound = matchesWithLicense(comment.getContent());
				} else {
					break;
				}
			}
		}
		if (!licenseFound) {
			if (REFORMAT_ACTION.equals(action)) {
				if (comments == null) {
					comments = new LinkedList<Comment>();
					cu.setComments(comments);
				}
				BlockComment licenseComment = new BlockComment(licenseContent);
				comments.add(0, licenseComment);
				ctx.addTransformationMessage("Missing license as block comment. License file added");
			} else if (CHECK_ACTION.equals(action)) {
				ctx.addTransformationMessage("Missing license as block comment. License file added");
			}
		}
		if (REMOVE_ACTION.equals(action)) {
			if (comments != null) {
				Iterator<Comment> it = comments.iterator();
				while (it.hasNext()) {
					comment = it.next();
					if (referenceIsPackage || comment instanceof BlockComment) {
						if (reference == null
								|| comment.isPreviousThan(reference)) {
							it.remove();
						} else {
							break;
						}
					}
				}
				if (comments.isEmpty()) {
					cu.setComments(null);
				}
			}
			ctx.addTransformationMessage("License removed");
		}
		if (UPDATE_ACTION.equals(action)) {
			if (comments != null) {
				Iterator<Comment> it = comments.iterator();
				while (it.hasNext()) {
					comment = it.next();
					if (referenceIsPackage || comment instanceof BlockComment) {
						if (reference == null
								|| comment.isPreviousThan(reference)) {
							it.remove();
						} else {
							break;
						}
					}
				}
			} else {
				comments = new LinkedList<Comment>();
				cu.setComments(comments);
			}
			BlockComment licenseComment = new BlockComment(licenseContent);
			comments.add(0, licenseComment);
			ctx.addTransformationMessage("Updating license");
		}
	}

	public void setLicenseFile(String path) throws FileNotFoundException {
		setLicense(new File(path));
	}

	public void setLicense(File licenseFile) throws FileNotFoundException {
		this.licenseFile = licenseFile;
		if (licenseFile == null) {
			throw new WalkModException("Missing license file");
		}
		if (!licenseFile.exists()) {
			throw new WalkModException("License file does not exists");
		}
		if (!licenseFile.canRead()) {
			throw new WalkModException("License file cannot be read");
		}
		StringBuffer sb = new StringBuffer();
		FileReader fr = new FileReader(licenseFile);
		try {
			BufferedReader reader = new BufferedReader(fr);
			try {
				String line = reader.readLine();
				List<String[]> wordLines = new LinkedList<String[]>();
				int wordsSize = 0;
				while (line != null) {
					sb.append(' ');
					String[] words = line.split("\\s");
					for (int i = 0; i < words.length; i++) {
						String trimmed = words[i].trim();

						if (isVariable(trimmed)) {
							String variable = trimmed.substring(2,
									trimmed.length() - 1);
							if (propertyValues.containsKey(variable)) {
								sb.append(propertyValues.get(variable));
							} else {
								sb.append(words[i]);
							}
						} else {
							sb.append(words[i]);
						}
						if (i + 1 < words.length) {
							sb.append(' ');
						}
						words[i] = trimmed;
						if (!"".equals(trimmed)) {
							wordsSize += 1;
						}
					}

					wordLines.add(words);
					line = reader.readLine();
					if (line != null) {
						sb.append('\n');
					}
				}
				licenseWords = new String[wordsSize];
				int i = 0;
				for (String[] words : wordLines) {
					for (int j = 0; j < words.length; j++) {
						if (!"".equals(words[j])) {
							licenseWords[i] = words[j];
							i++;
						}
					}
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (IOException e) {
			throw new WalkModException("License file read", e);
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				throw new WalkModException("License file cannot be closed", e);
			}
		}
		licenseContent = new String(sb);
	}

	public void setPropertyValues(Map<String, String> propertyValues) {
		this.propertyValues = propertyValues;
	}
}
