package net.ripe.db.nrtm4;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class NrtmFileRepo {

    private final String path;

    NrtmFileRepo(
        @Value("${nrtm.file.path:/tmp}") String path
    ) {
        this.path = path;
    }

    boolean checkIfFileExists(final String name) {
        return false;
    }
}
