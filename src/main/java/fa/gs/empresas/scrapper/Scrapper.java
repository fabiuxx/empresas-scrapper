/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fa.gs.empresas.scrapper;

import fa.gs.utils.collections.Lists;
import fa.gs.utils.collections.Maps;
import fa.gs.utils.misc.Assertions;
import fa.gs.utils.misc.Units;
import fa.gs.utils.misc.text.Joiner;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Fabio A. González Sosa
 */
public class Scrapper {

    public static Collection<Map<String, String>> scrap(String empresa, String ciudad) throws Throwable {
        Collection<Map<String, String>> DATA = Lists.empty();
        int pageNum = 1;
        while (true) {
            String url = buildUrl(empresa, ciudad, pageNum);
            Collection<Map<String, String>> data = scrapList(url);
            if (data.isEmpty()) {
                break;
            }
            for (Map<String, String> data0 : data) {
                DATA.add(data0);
            }
            pageNum++;
        }
        return DATA;
    }

    private static Collection<Map<String, String>> scrapList(String url) throws Throwable {
        Collection<Map<String, String>> datas = Lists.empty();
        Home.INSTANCE.log("<<<< %s", url);
        try {
            Document doc = Jsoup.connect(url).get();
            Elements uls = doc.selectXpath("//ul[@class=\"lista-datos-empresa\"]");
            for (Element ul : uls) {
                if ("lista-datos-empresa".equals(ul.attr("class"))) {
                    Elements lis = ul.getElementsByTag("li");
                    String pageUrl = readPageUrl(lis.get(lis.size() - 1));
                    Map<String, String> data = scrapPage(pageUrl);
                    data.put("Empresa", lis.get(0).text().split(":")[1].trim());
                    data.put("Rubro", readRubro(lis.get(2)));
                    data.put("Origen", pageUrl);
                    datas.add(data);
                }
            }
        } catch (Throwable thr) {
            if (thr instanceof HttpStatusException) {
                Home.INSTANCE.log("[ADVERTENCIA] Se retornó el código: %s.", ((HttpStatusException) thr).getStatusCode());
            } else {
                throw thr;
            }
        }
        return datas;
    }

    private static Map<String, String> scrapPage(String url) throws Throwable {
        Map<String, String> data = Maps.empty();
        Home.INSTANCE.log("**** %s", url);
        try {
            Document doc = Jsoup.connect(url).get();
            Elements nodes = doc.selectXpath("//div[@class=\"laempresa\"]");
            for (Element node : nodes) {
                Elements lis = node.selectXpath(".//li");
                for (Element li : lis) {
                    List<String> parts = Arrays.asList(li.text().split(":"));
                    parts.stream().forEach(e -> e.trim());
                    String k = Units.executeFn("", () -> parts.get(0).trim());
                    String v = Units.executeFn("", () -> parts.get(1).trim());
                    data.put(k, v);
                }
            }
        } catch (Throwable thr) {
            if (thr instanceof HttpStatusException) {
                Home.INSTANCE.log("Advertencia. Se retornó el código: %s.", ((HttpStatusException) thr).getStatusCode());
            } else {
                throw thr;
            }
        }
        return data;
    }

    private static String readPageUrl(Element el) {
        return el.selectXpath(".//a").get(0).attr("href").trim();
    }

    private static String readRubro(Element el) {
        List<String> parts = Units.executeFn(() -> Arrays.asList(el.text().split(":")[1].split((","))));
        if (Assertions.isNullOrEmpty(parts)) {
            return "";
        } else {
            parts.stream().forEach(String::trim);
            return Joiner.of(parts).separator(",").join();
        }
    }

    private static String buildUrl(String empresa, String ciudad, int pageNum) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("https://guiaindustrialcomercial.com.py/buscar2?_q=%s", System.currentTimeMillis()));
        if (!Assertions.stringNullOrEmpty(empresa)) {
            builder.append("&name=").append(URLEncoder.encode(empresa, "UTF-8"));
        }
        if (!Assertions.stringNullOrEmpty(ciudad)) {
            builder.append("&city=").append(URLEncoder.encode(ciudad, "UTF-8"));
        }
        builder.append("&page=").append(pageNum);
        return builder.toString();
    }

}
