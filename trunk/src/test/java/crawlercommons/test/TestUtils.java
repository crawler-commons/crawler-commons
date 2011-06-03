package crawlercommons.test;

import crawlercommons.fetcher.UserAgent;

public class TestUtils {
    
    // User agent for when we're not doing external fetching, so we just need a fake name.
    public static final UserAgent CC_TEST_AGENT = new UserAgent("test", "test@domain.com", "http://test.domain.com");

}
