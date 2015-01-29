package net.ripe.db.whois.api.generator;

import com.google.common.base.Objects;

import java.io.File;

public class TemplateFileSpecification {
    private String directory;
    private String filename;
    private File file = null;

    private String mainMethod;

    public TemplateFileSpecification(String templateDirectory, String templateFilename) {
        super();
        this.directory = templateDirectory;
        this.filename = templateFilename;
        this.mainMethod = "main";
        this.file = new File(this.directory + File.separator + this.filename);
    }

    public TemplateFileSpecification(String templateDirectory, String templateFilename, String mainMethod) {
        super();
        this.directory = templateDirectory;
        this.filename = templateFilename;
        this.mainMethod = mainMethod;
        this.file = new File(this.directory + File.separator + this.filename);
    }

    public String getDirectory() {
        return directory;
    }

    public String getFilename() {
        return filename;
    }

    public String getMainMethod() {
        return mainMethod;
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("absPath", file.getAbsolutePath())
                .add("mainMethod", this.mainMethod).toString();
    }

}
