package crawlercommons.filters;

public interface URLFilter {

    /**
     * Returns a modified version of the input URL or null if the URL should be
     * removed
     **/
    public String filter(String urlString);

}
