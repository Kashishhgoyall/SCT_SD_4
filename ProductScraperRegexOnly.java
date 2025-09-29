import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductScraperRegexOnly {

    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // Fetch HTML content of a URL
    private static String fetchHTML(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line).append("\n");
        }
        in.close();
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter product page URL: ");
        String url = sc.nextLine().trim();

        try {
            String html = fetchHTML(url);

            // Regex patterns for Books to Scrape
            Pattern productPattern = Pattern.compile("<article class=\"product_pod\">(.*?)</article>", Pattern.DOTALL);
            Pattern namePattern = Pattern.compile("title=\"(.*?)\"");
            Pattern pricePattern = Pattern.compile("<p class=\"price_color\">(.*?)</p>");
            Pattern ratingPattern = Pattern.compile("<p class=\"star-rating (.*?)\">");

            Matcher productMatcher = productPattern.matcher(html);

            try (FileWriter writer = new FileWriter("products_regex_only.csv")) {
                writer.append("Name,Price,Rating\n");

                while (productMatcher.find()) {
                    String productBlock = productMatcher.group(1);

                    Matcher nameMatcher = namePattern.matcher(productBlock);
                    String name = nameMatcher.find() ? nameMatcher.group(1) : "";

                    Matcher priceMatcher = pricePattern.matcher(productBlock);
                    String price = priceMatcher.find() ? priceMatcher.group(1) : "";

                    Matcher ratingMatcher = ratingPattern.matcher(productBlock);
                    String rating = ratingMatcher.find() ? ratingMatcher.group(1) : "";

                    writer.append(csvEscape(name)).append(",")
                          .append(csvEscape(price)).append(",")
                          .append(csvEscape(rating)).append("\n");
                }
            }

            System.out.println("✅ Scraping completed using regex only! Data saved in products_regex_only.csv");

        } catch (IOException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
