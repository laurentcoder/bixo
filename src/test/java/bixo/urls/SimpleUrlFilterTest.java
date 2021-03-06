package bixo.urls;

import org.junit.Assert;
import org.junit.Test;

import bixo.datum.UrlDatum;
import bixo.urls.BaseUrlFilter;
import bixo.urls.SimpleUrlFilter;


public class SimpleUrlFilterTest {

    @Test
    public void testValidUrls() {
        BaseUrlFilter urlFilter = new SimpleUrlFilter();
        Assert.assertFalse(urlFilter.isRemove(new UrlDatum("http://domain.com")));
    }
    
    @Test
    public void testInvalidUrls() {
        BaseUrlFilter urlFilter = new SimpleUrlFilter();
        Assert.assertTrue("No protocol", urlFilter.isRemove(new UrlDatum("www.domain.com")));
        Assert.assertTrue("Unknown protocol", urlFilter.isRemove(new UrlDatum("mdata://www.domain.com")));
        Assert.assertTrue("Invalid port", urlFilter.isRemove(new UrlDatum("http://www.domain.com:a")));
    }
}
