package net.ripe.db.whois.api.generator;

import net.ripe.db.whois.common.rpsl.ObjectTemplate;

import java.io.IOException;

public class MyCodeGenerator {
    public static final String PACKAGE_NAME = "nl.grol.whois.data.model";
    private String templateDirectory = null;

    public MyCodeGenerator(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public static void main(String[] args) throws IOException {
        MyCodeGenerator codeGenerator = new MyCodeGenerator(
                "./whois-api/src/main/java/" +
                        "/net/ripe/db/whois/api/generator");
        codeGenerator.generateAll();
    }

    public void generateAll() throws IOException {
        generateEntities();
    }

    private void generateEntities() throws IOException {
        System.out.println("generate entities: start");

        for (ObjectTemplate ot : ObjectTemplate.getTemplates()) {

            SingleFileGenerator generator =
                    new SingleFileGenerator(
                            new TemplateFileSpecification(this.templateDirectory, "class.stg"),
                            new TargetFileSpecification("./whois-api/src/main/java/",
                                    PACKAGE_NAME, asJavaClassName(ot.getObjectType().getName()) + ".java"));
            generator.addContext("package", PACKAGE_NAME);
            generator.addContext("struct", ot);

            generator.generate();
        }

        System.out.println("generate entities: end");
    }

    private String asJavaClassName(String in) {
        return toFirstUpper(in.replaceAll("-", ""));
    }

    public String toFirstUpper(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }

    public String toFirstLower(String in) {
        return in.substring(0, 1).toLowerCase() + in.substring(1);
    }
}
