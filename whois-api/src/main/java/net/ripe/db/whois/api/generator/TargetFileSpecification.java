package net.ripe.db.whois.api.generator;

import com.google.common.base.Objects;

import java.io.File;
import java.io.IOException;

public class TargetFileSpecification {
    private String baseDirectory;
    private String packageName;
    private String filename;

    public TargetFileSpecification(String baseDirectory, String packageName, String templateFilename) {
        super();
        this.baseDirectory = baseDirectory;
        this.packageName = packageName;
        this.filename = templateFilename;
    }

    public File getFile() throws IOException {
        File dir = null;

        if (packageName != null) {
            /*
			 * Convert package-name to directory
			 */
            String packagePath = this.packageName.replace(".", File.separator);
            dir = new File(baseDirectory + File.separator + packagePath);
        } else {
            dir = new File(baseDirectory);
        }
		
		/*
		 * Make sure directory exists
		 */
        dir.mkdirs();
		
		/*
		 * Return file to write to
		 */
        return new File(dir + File.separator + filename);
    }

    public String toString() {
        return Objects.toStringHelper(this)
                .add("baseDirectory", this.baseDirectory)
                .add("filename", this.filename)
                .add("absPath", new File(this.baseDirectory + File.separator + this.filename))
                .add("packageName", this.packageName)
                .toString();
    }

}
