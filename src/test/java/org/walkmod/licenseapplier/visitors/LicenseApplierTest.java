package org.walkmod.licenseapplier.visitors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.walkers.VisitorContext;

public class LicenseApplierTest {

	@Test
	public void testUpdateFilesWithNoLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("public class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("update");
		visitor.setLicense(new File("src/test/resources/license.txt"));
		visitor.visit(cu, new VisitorContext());
		Assert.assertNotNull(cu.getComments());
		Assert.assertTrue(cu.toString().contains("2015"));
	}

	@Test
	public void testUpdateFilesWithDifferentLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("/*\n Copyright (C) 2013 Raquel Pau.\n" +

		"Walkmod is free software: you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU Lesser General Public License as published by\n"
				+ "the Free Software Foundation, either version 3 of the License, or\n"
				+ "(at your option) any later version.\n" +

				"Walkmod is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
				+ "GNU Lesser General Public License for more details.\n" +

				"You should have received a copy of the GNU Lesser General Public License\n"
				+ "along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/\npublic class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("update");
		visitor.setLicense(new File("src/test/resources/license.txt"));
		visitor.visit(cu, new VisitorContext());
		Assert.assertNotNull(cu.getComments());
		Assert.assertEquals(1, cu.getComments().size());
		Assert.assertTrue(cu.toString().contains("2015"));
	}

	@Test
	public void testReformatFilesWithNoLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("public class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("reformat");
		visitor.setLicense(new File("src/test/resources/license.txt"));
		visitor.visit(cu, new VisitorContext());
		Assert.assertNotNull(cu.getComments());
	}

	@Test
	public void testReformatFilesWithDifferentLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("/*\n Copyright (C) 2013 Raquel Pau.\n" +

		"Walkmod is free software: you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU Lesser General Public License as published by\n"
				+ "the Free Software Foundation, either version 3 of the License, or\n"
				+ "(at your option) any later version.\n" +

				"Walkmod is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
				+ "GNU Lesser General Public License for more details.\n" +

				"You should have received a copy of the GNU Lesser General Public License\n"
				+ "along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/\npublic class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("reformat");
		visitor.setLicense(new File("src/test/resources/license.txt"));
		visitor.visit(cu, new VisitorContext());
		Assert.assertNotNull(cu.getComments());
		Assert.assertEquals(1, cu.getComments().size());
		Assert.assertTrue(cu.toString().contains("2013"));
	}

	@Test
	public void testRemoveLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("/*\n Copyright (C) 2013 Raquel Pau.\n" +

		"Walkmod is free software: you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU Lesser General Public License as published by\n"
				+ "the Free Software Foundation, either version 3 of the License, or\n"
				+ "(at your option) any later version.\n" +

				"Walkmod is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
				+ "GNU Lesser General Public License for more details.\n" +

				"You should have received a copy of the GNU Lesser General Public License\n"
				+ "along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/\npublic class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("remove");
		visitor.visit(cu, new VisitorContext());
		Assert.assertNull(cu.getComments());
	}

	@Test
	public void testCheckLicense() throws Exception {
		CompilationUnit cu = ASTManager.parse("public class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("check");
		visitor.setLicense(new File("src/test/resources/license.txt"));
		VisitorContext ctx = new VisitorContext();

		visitor.visit(cu, ctx);
		Assert.assertNull(cu.getComments());

		Assert.assertEquals("Missing license as block comment. License file added", ctx.getVisitorMessages().iterator()
				.next());
	}
	
	@Test
	public void testLicensePatterns() throws Exception {
		
		CompilationUnit cu = ASTManager.parse("public class Foo {}");
		LicenseApplier visitor = new LicenseApplier();
		visitor.setAction("reformat");
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("year", "2015");
		properties.put("author", "Raquel Pau");
		
		visitor.setLicense(new File("src/test/resources/license-pattern.txt"));
		visitor.setPropertyValues(properties);
		
		
		visitor.visit(cu, new VisitorContext());
		Assert.assertNotNull(cu.getComments());
		
		Assert.assertTrue(cu.toString().contains("2015"));
	}

}
