#!/usr/bin/env python

import sys
import json
import urllib2
import traceback

RELS = [
    "about", "alternate", "appendix", "archives", "author", "bookmark",
    "canonical", "chapter", "collection", "contents", "copyright",
    "create-form", "current", "describedby", "describes", "disclosure",
    "duplicate", "edit", "edit-form", "edit-media", "enclosure", "first",
    "glossary", "help", "hosts", "hub", "icon", "index", "item", "last",
    "latest-version", "license", "lrdd", "monitor", "monitor-group",
    "next", "next-archive", "nofollow", "noreferrer", "payment",
    "predecessor-version", "prefetch", "prev", "preview", "previous",
    "prev-archive", "privacy-policy", "profile", "related", "replies",
    "search", "section", "self", "service", "start", "stylesheet",
    "subsection", "successor-version", "tag", "terms-of-service", "type",
    "up", "version-history", "via", "working-copy", "working-copy-of"
]

class JsonTests(object):
    def __init__(self, response):
        self.response = json.loads(response)

    def run(self):
        m = [_ for _ in dir(self) if _.startswith('test_')]
        for _ in m:
            try:
                self.__getattribute__(_)()
            except AssertionError,e:
                j, j, tb = sys.exc_info()
                print 'fail:', _ + ':', traceback.extract_tb(tb)[-1][3]

    def test_isdict(self):
        assert isinstance(self.response, dict)

    def test_conformance(self):
        assert 'rdapConformance' in self.response
        conformance = self.response['rdapConformance']
        assert isinstance(conformance, list)
        assert len(conformance) == 1    # only one valid value
        assert 'rdap_level_0' in conformance
        assert [_ for _ in conformance if not isinstance(_, basestring)] == []

    def test_links(self):
        assert 'links' in self.response         # must have links for this test
        links = self.response['links']
        assert isinstance(links, list)
        for _ in links:
            assert isinstance(_, dict)
            assert set(_.keys()) <= {u"rel", u"href", u"hreflang", u"title", u"media", u"type", u"value"}
            assert set(_.keys()) >= {u"rel", u"value", u"href"}
            assert _['rel'] in RELS

            # for APNIC, we actually also ONLY allow those three flags
            assert set(_.keys()) == {u"rel", u"value", u"href"}
            # for APNIC, we only permit "self"
            assert _['rel'] in [u'self']
            # for APNIC, both value and href should start with http://rdap.apnic.net/
            #assert _['href'].startswith('http://rdap.apnic.net/')
            #assert _['value'].startswith('http://rdap.apnic.net/')
            # since rel is self, href and value should be equal
            assert _['href'] == _['value']

        # for APNIC, there must always be one and only one links element
        assert len(links) == 1 

    def test_notices(self):
        assert 'notices' in self.response               # must have 'notices' with T&C
        notices = self.response['notices']
        assert isinstance(notices, list)
        assert len(notices) == 1
        assert isinstance(notices[0], dict)
        assert set(notices[0].keys()) == {u'title', u'description'} #, u'links'}
        assert notices[0]['title'] == u'Terms and Conditions'
        #assert notices[0]['description'] == [u'Whois data copyright terms']
        # TODO: should contain http://www.apnic.net/db/dbcopyright.html

class EntityTests(JsonTests):
    def __init__(self, response):
        super(EntityTests, self).__init__(response)

if __name__ == '__main__':
    tests = [
        [ EntityTests, 'http://127.0.0.1:55991/rdap/entity/ORG-TEST1-TEST' ]
    ]
    for test in tests:
        response = urllib2.urlopen(test[1])
        jj = response.read()
        tt = test[0](jj)
        tt.run()
