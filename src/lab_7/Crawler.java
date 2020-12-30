package lab_7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Crawler
{

    static final String A_REF_START = "<a href=\"";
    static final String A_REF_END = "\">";

    static List<Link> scanLink(URL url0, int maxDepth)
    {
        Link link = new Link(url0, 0);
        List<Link> linksToScan = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        linksToScan.add(link);

        while (linksToScan.size() > 0)
        {
            Link current = linksToScan.remove(0);
            links.add(current);
            if (current.depth >= maxDepth)
            {
                continue;
            }

            try
            {
                getLinks(current.url).stream().forEach((l) ->
                {
                    URL url = null;
                    try
                    {
                        url = new URL(l);
                    } catch (MalformedURLException e) {}
                    if (url != null) {
                        linksToScan.add(new Link(url, current.depth + 1));
                        //System.out.println(">>> " + url + "\t" + (current.depth + 1));
                    }


                });
            } catch (Exception e) {}
        }

        return links;
    }

    public static Optional<String[]> getARefFirst(String s)
    {
        // <a href="[любой_URL-адрес_начинающийся_с_http://]">

        int start = s.indexOf(A_REF_START);
        if (start == -1) return Optional.empty();
        int end = s.indexOf("\"", start + A_REF_START.length());
        if (end == -1) return Optional.empty();

        String ref = s.substring(start + A_REF_START.length(), (end - "\"".length() + 1));
        String tail = s.substring(end);

        return Optional.of(new String[] { ref, tail });
    }

    public static List<String> getLinks(URL url) throws IOException
    {
        List<String> links = new ArrayList<>();
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(1000);

        try(InputStream is = conn.getInputStream())
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            reader.lines().forEach((s) ->
            {
                Optional<String[]> link = getARefFirst(s);
                while (link.isPresent())
                {
                    String linkStr = link.get()[0];

                    if (linkStr.startsWith("https://"))
                    {
                        links.add(linkStr);
                    }
                    s = link.get()[1];
                    link = getARefFirst(s);
                }
            });
        }
        return links;
    }
}
