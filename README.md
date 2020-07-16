[![Build Status](https://api.travis-ci.org/crawler-commons/crawler-commons.svg?branch=master)](https://travis-ci.org/crawler-commons/crawler-commons)
[![license](https://img.shields.io/github/license/crawler-commons/crawler-commons.svg?maxAge=2592000?style=plastic)](http://www.apache.org/licenses/LICENSE-2.0)

# Overview

Crawler-Commons is a set of reusable Java components that implement functionality common to any web crawler. These components benefit from collaboration among various existing web crawler projects, and reduce duplication of effort.

# User Documentation

## Javadocs
* [1.1](http://crawler-commons.github.io/crawler-commons/1.1/)
* [1.0](http://crawler-commons.github.io/crawler-commons/1.0/)
* [0.10](http://crawler-commons.github.io/crawler-commons/0.10/)
* [0.9](http://crawler-commons.github.io/crawler-commons/0.9/)
* [0.8](http://crawler-commons.github.io/crawler-commons/0.8/)
* [0.7](http://crawler-commons.github.io/crawler-commons/0.7/)
* [0.6](http://crawler-commons.github.io/crawler-commons/0.6/apidocs/)

# Mailing List

There is a mailing list on [Google Groups](https://groups.google.com/forum/?fromgroups#!forum/crawler-commons).

# Issue Tracking

If you find an issue, please file a report [here](https://github.com/crawler-commons/crawler-commons/issues)

# Crawler-Commons News

## 29th June 2020  - crawler-commons 1.1 released

We are glad to announce the 1.1 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-1.1/CHANGES.txt) file included with the release for a full list of details.

## 21st March 2019  - crawler-commons 1.0 released

We are glad to announce the 1.0 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-1.0/CHANGES.txt) file included with the release for a full list of details.
Among other bug fixes and improvements this version adds support for parsing sitemap extensions (image, video, news, alternate links).

## 7th June 2018  - crawler-commons 0.10 released

We are glad to announce the 0.10 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.10/CHANGES.txt) file included with the release for a full list of details.
This version contains among other things improvements to the Sitemap parsing and the removal of the Tika dependency. 

## 31st October 2017  - crawler-commons 0.9 released

We are glad to announce the 0.9 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.9/CHANGES.txt) file included with the release for a full list of details.
The main changes are the removal of DOM-based sitemap parser as the SAX equivalent introduced in the previous version has better performance and is also more robust. You might need to change your code to replace `SiteMapParserSAX` with `SiteMapParser`.
The parser is now aware of namespaces, and by default does not force the namespace to be the one recommended in the specification (`http://www.sitemaps.org/schemas/sitemap/0.9`) as variants can be found in the wild. You can set the behaviour using the method _setStrictNamespace(boolean)_.

As usual, the version 0.9 contains numerous improvements and bugfixes and all users are invited to upgrade to this version.

## 9th June 2017  - crawler-commons 0.8 released

We are glad to announce the 0.8 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.8/CHANGES.txt) file included with the release for a full list of details.
The main changes are the removal of the HTTP fetcher support, which has been put in a [separate project](https://github.com/crawler-commons/http-fetcher). We also added a SAX-based parser for processing sitemaps, which requires less memory 
and is more robust to malformed documents than its DOM-based counterpart. The latter has been kept for now but might be removed in the future.

## 24th November 2016  - crawler-commons 0.7 released

We are glad to announce the 0.7 release of Crawler-Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.7/CHANGES.txt) file included with the release for a full list of details.
The main changes are that Crawler-Commons now requires JAVA 8 and that the package crawlercommons.url has been replaced with crawlercommons.domains. If your project uses CC then you might want to run the following command on it

```
find . -type f -print0 | xargs -0 sed -i 's/import crawlercommons\.url\./import crawlercommons\.domains\./'
```

Please note also that this is the last release containing the HTTP fetcher support, which is deprecated and will be removed from the next version.

The version 0.7 contains numerous improvements and bugfixes and all users are invited to upgrade to this version.


## 11th June 2015 - crawler-commons 0.6 is released

We are glad to announce the 0.6 release of Crawler Commons. See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.6/CHANGES.txt) file included with the release for a full list of details.

We suggest all users to upgrade to this version. Details of how to do so can be found on  [Maven Central](http://search.maven.org/#artifactdetails%7Ccom.github.crawler-commons%7Ccrawler-commons%7C0.6%7Cjar). Please note that the groupId has changed to com.github.crawler-commons.

The Java documentation can be found [here](http://crawler-commons.github.io/crawler-commons/0.6/apidocs/).

## 22nd April 2015 - crawler-commons has moved

The crawler-commons project is now being hosted at GitHub, due to the demise of Google code hosting.

## 15th October 2014 - crawler-commons 0.5 is released

We are glad to announce the 0.5 release of Crawler Commons. This release mainly improves Sitemap parsing as well as an upgrade to [Apache Tika 1.6](http://tika.apache.org).

See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/crawler-commons-0.5/CHANGES.txt) file included with the release for a full list of details. Additionally the Java documentation can be found [here](http://crawler-commons.googlecode.com/svn/wiki/javadoc/0.5/index.html).

We suggest all users to upgrade to this version. The Crawler Commons project artifacts are released as Maven artifacts and can be found at [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.google.code.crawler-commons%22%20AND%20a%3A%22crawler-commons%22).

## 11th April 2014 - crawler-commons 0.4 is released

We are glad to announce the 0.4 release of Crawler Commons. Amongst other improvements, this release includes support for Googlebot-compatible regular expressions in URL specifications, further imprvements to robots.txt parsing and an upgrade of httpclient to v4.2.6\.

See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/master/CHANGES.txt) file included with the release for a full list of details.

We suggest all users to upgrade to this version. Details of how to do so can be found on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.google.code.crawler-commons%22%20AND%20a%3A%22crawler-commons%22).

## 11 Oct 2013 - crawler-commons 0.3 is released

This release improves robots.txt and sitemap parsing support, updates Tika to the latest released version (1.4), and removes some left-over cruft from the pre-Maven build setup.

See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/master/CHANGES.txt) file included with the release for a full list of details.

## 24 Jun 2013 - Nutch 1.7 now uses crawler-commons for robots.txt parsing

Similar to the previous note about Nutch 2.2, there's now a version of Nutch in the 1.x tree that also uses crawler-commons. See [Apache Nutch v1.7 Released](http://nutch.apache.org/#24th+June+2013+-+Apache+Nutch+v1.7+Released) for more details.

## 08 Jun 2013 - Nutch 2.2 now uses crawler-commons for robots.txt parsing

See [Apache Nutch v2.2 Released](http://nutch.apache.org/#08+June+2013+-+Apache+Nutch+v2.2+Released) for more details.

## 02 Feb 2013 - crawler-commons 0.2 is released

This release improves robots.txt and sitemap parsing support.

See the [CHANGES.txt](https://github.com/crawler-commons/crawler-commons/blob/master/CHANGES.txt) file included with the release for a full list of details.
