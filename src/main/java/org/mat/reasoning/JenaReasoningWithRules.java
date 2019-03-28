package org.mat.reasoning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class JenaReasoningWithRules {

	public static void main(String[] args) throws IOException {
		
		Model model = ModelFactory.createDefaultModel();
		model.read("dataset.n3");

		Reasoner reasoner = new GenericRuleReasoner(Rule.rulesFromURL("rules.txt"));

		InfModel infModel = ModelFactory.createInfModel(reasoner, model);

		// print out the statements in the model
		StmtIterator it = infModel.listStatements();

		while (it.hasNext()) {
			Statement stmt = it.nextStatement();

			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			System.out.println(subject.toString() + " " + predicate.toString() + " " + object.toString());
		}

		boolean cont = infModel.contains(new ResourceImpl("http://nordea.com/arc/openshift#Service1"),
				new PropertyImpl("http://nordea.com/arc/openshift#isOn"),
				new ResourceImpl("http://nordea.com/arc/openshift#Server1"));

		System.out.println("Inferred => " + cont);
		
		Model shapeModel = JenaUtil.createDefaultModel();
	    shapeModel.read("shape.ttl");
	      
		Resource reportResource = ValidationUtil.validateModel(infModel, shapeModel, true);
		
		boolean conforms  = reportResource.getProperty(SH.conforms).getBoolean();
		System.out.println("Conforms => " + conforms);
		
		if (!conforms) {
			String report = "report.ttl";
			File reportFile = new File(report);
			reportFile.createNewFile();     
			OutputStream reportOutputStream = new FileOutputStream(reportFile);
      
			RDFDataMgr.write(reportOutputStream, reportResource.getModel(), RDFFormat.TTL);
		}
	}
}
