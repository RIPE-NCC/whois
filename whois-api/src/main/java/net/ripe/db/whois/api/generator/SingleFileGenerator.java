package net.ripe.db.whois.api.generator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.ErrorBuffer;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SingleFileGenerator {

    private TemplateFileSpecification templateSpec;
    private TargetFileSpecification targetSpec;
    private Map<String, Object> args = new LinkedHashMap<String, Object>();

    public SingleFileGenerator(TemplateFileSpecification template, TargetFileSpecification target) {
        super();

        this.templateSpec = template;
        this.targetSpec = target;

        System.out.println(this.toString());
    }

    public SingleFileGenerator addContext(String key, Object value) {
        args.put(key, value);
        return this;
    }

    public void generate() throws IOException {
        /*
		 *  this where the templateSpec files are located
		 */
        STGroup group = getTemplateGroup(templateSpec.getDirectory(), templateSpec.getFilename());
        Preconditions.checkNotNull(group);

		/*
		 *  extract your main-hook method from the templateSpec
		 */
        ST template = group.getInstanceOf(this.templateSpec.getMainMethod());
        Preconditions.checkNotNull(template);

		/*
		 *  pass parameters to main-hook: Order does matter so use LinkedHashMap
		 */
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            template.add(key, value);
        }
		
		/*
		 *  use the templateSpec to write targetSpec file
		 */
        ErrorBuffer ebuff = new ErrorBuffer();
        template.write(targetSpec.getFile(), ebuff, "UTF-8");
    }

    private STGroup getTemplateGroup(String templateDirectory, String templateFileName) {
        STGroup group = new STGroupFile(templateDirectory + File.separator + templateFileName);
        Preconditions.checkNotNull(group);

        return group;
    }

    public String toString() {

        return Objects.toStringHelper(this)
                .add("templateSpec", this.templateSpec)
                .add("targetSpec", this.targetSpec)
                .toString();
    }

}
