package net.ripe.db.whois.api.autocomplete;

import org.springframework.stereotype.Component;

/**
 * Autocomplete - Suggestions - Typeahead API
 */
@Component
public class AutocompleteService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteService.class);
//
//    private final FreeTextIndex freeTextIndex;
//    private final AnalyzingInfixSuggester suggester;
//
//    @Autowired
//    public AutocompleteService(final FreeTextIndex freeTextIndex) throws IOException {
//        this.freeTextIndex = freeTextIndex;
//        this.suggester = freeTextIndex.createSuggester();
//    }
//
//    public void lookup(final String request, final PrintWriter writer) throws Exception {
//        LOGGER.info("lookup: {}", request);
//
//
//        suggester.build(new DocumentDictionary(freeTextIndex.getReader(), "primary-key", "primary-key"));
//
//        final String name = "AA";
//
//       try {
//            List<Lookup.LookupResult> results;
//            HashSet<BytesRef> contexts = new HashSet<BytesRef>();
//            contexts.add(new BytesRef("primary-key".getBytes("UTF8")));
//
//            // Do the actual lookup.  We ask for the top 2 results.
//
//            final int hits = 1;
//            final boolean doHighlights = false;
//           final boolean allTermsRequired = false;
//
//            results = suggester.lookup(name, hits, allTermsRequired, doHighlights);
//
//           LOGGER.info("results = {}", results.size());
//
//            for (Lookup.LookupResult result : results) {
//                LOGGER.info("RESULT = " + result.key);
//            }
//        } catch (IOException e) {
//            LOGGER.error(e.getMessage(), e);
//        }
//
//    }

}
