package spec.integration

import net.ripe.db.whois.common.rpsl.RpslObject

// TODO [AK] Now that we also have access to query here, we can expand our tests
abstract class BaseWhoisSourceSpec extends BaseSpec {
    def setupSpec() {
        whoisFixture.start()
    }

    def setup() {
        setupObjects(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })
    }

    def cleanupSpec() {
        if (whoisFixture != null) {
            try {
                whoisFixture.stop()
            } catch (Exception e) {
                e.printStackTrace()
            }
        }
    }

    abstract Map<String, String> getFixtures()
}
