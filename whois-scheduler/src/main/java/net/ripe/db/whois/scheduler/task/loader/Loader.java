package net.ripe.db.whois.scheduler.task.loader;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

abstract class Loader {

    protected final JdbcTemplate whoisTemplate;
    protected final ObjectLoader objectLoader;

    public Loader(final DataSource dataSource, ObjectLoader objectLoader) {
        this.objectLoader = objectLoader;
        this.whoisTemplate = new JdbcTemplate(dataSource);
    }

    public String loadSplitFiles(String... filenames) {
        Result result = new Result();
        try {
            loadSplitFiles(result, filenames);
        } catch (Exception e) {
            result.addText(String.format("\n%s\n", e.getMessage()));
        } finally {
            result.addText(String.format("FINISHED\n%d succeeded\n%d failed in pass 1\n%d failed in pass 2\n",
                    result.getSuccess(), result.getFailPass1(), result.getFailPass2()));

            return result.toString();
        }
    }

    abstract void resetDatabase() ;

    abstract void loadSplitFiles(Result result, String... filenames);

    abstract void runPass(final Result result, final String filename, final int pass);

}
