package com.codecrate.testdox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Creates an "human readable" html report of unit test cases.
 * To enable this report, the following configuration should be added to the pom.xml
 * <pre>
 *   &lt;reporting&gt;
 *     &lt;plugins&gt;
 *       &lt;plugin&gt;
 *         &lt;groupId&gt;com.codecrate.maven.plugins&lt;/groupId&gt;
 *         &lt;artifactId&gt;testdox-maven-plugin&lt;/artifactId&gt;
 *       &lt;/plugin&gt;
 *     &lt;/plugins&gt;
 *   &lt;/reporting&gt;
 * </pre>
 *
 * @goal report
 * @phase site
 */
public class TestDoxReportMojo extends AbstractMavenReport {
    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject mavenProject;

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * @parameter expression="${component.org.apache.maven.doxia.siterenderer.Renderer}"
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    private TestDoxFormatter formatter = new TestDoxFormatter();

	protected void executeReport(Locale locale) throws MavenReportException {
		Sink sink = getSink();
        sink.head();
        sink.title();
        sink.text("TestDox Report");
        sink.title_();
        sink.head_();

		sink.body();

		getLog().debug("testdox report output is set to: " + getOutputDirectory());

		String testSourceDirectory = mavenProject.getBuild().getTestSourceDirectory();
		getLog().info("Processing test classes from: " + testSourceDirectory);

		JavaDocBuilder builder = new JavaDocBuilder();
		builder.addSourceTree(new File(testSourceDirectory));
		JavaSource[] sources = builder.getSources();
		for (int i = 0; i < sources.length; i++) {
			JavaSource source = sources[i];

			for (Iterator iter = getTestClasses(source).iterator(); iter.hasNext();) {
				JavaClass clazz = (JavaClass) iter.next();

				getLog().info("Processing test class: " + clazz.getName());

				sink.section1();
				sink.sectionTitle1();
				sink.text(formatter.prettifyTestClassName(clazz.getName()));
				sink.sectionTitle1_();
				sink.section1_();

				sink.list();

				for (Iterator iterator = getTestMethods(clazz).iterator(); iterator.hasNext();) {
					JavaMethod method = (JavaMethod) iterator.next();

					sink.listItem();
					sink.text(formatter.prettifyTestMethodName(method.getName()));
					sink.listItem_();
				}
				sink.list_();
			}
		}

		sink.body_();
		sink.flush();
		sink.close();
	}

    private Collection getTestMethods(JavaClass aClass) {
    	Collection results = new ArrayList();
		JavaMethod[] methods = aClass.getMethods();
		for (int k = 0; k < methods.length; k++) {
			JavaMethod method = methods[k];
			if (formatter.isATestMethod(method)) {
				results.add(method);
			}
		}
		return results;
	}

	private Collection getTestClasses(JavaSource source) {
    	Collection results = new ArrayList();
    	JavaClass[] classes = source.getClasses();
		for (int j = 0; j < classes.length; j++) {
            JavaClass clazz = classes[j];
            if (formatter.isTestClass(clazz)) {
            	results.add(clazz);
            }
		}

		return results;
	}

	public String getDescription(Locale locale) {
		return "TestDox Report Description";
	}

	public String getName(Locale locale) {
		return "TestDox Report";
	}

	protected String getOutputDirectory() {
		return outputDirectory;
	}

	protected MavenProject getProject() {
		return mavenProject;
	}

	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	public String getOutputName() {
		return "testdox";
	}
}
