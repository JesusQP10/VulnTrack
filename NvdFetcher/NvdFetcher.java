import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.file.*;

public class NvdFetcher {
    
    static final String API_KEY = "8CE3D2E8-7053-F111-836B-129478FCB64D";
    static final String OUTPUT_FILE = "C:\\DataFlex Projects\\VulnTrackApp\\nvd_result.txt";
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Uso: NvdFetcher <CVE-ID>");
            System.exit(1);
        }
        
        String cveId = args[0];
        String urlStr = "https://services.nvd.nist.gov/rest/json/cves/2.0?cveId=" + cveId;
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("nvd-api-key", API_KEY);
        conn.setRequestProperty("User-Agent", "VulnTrack/1.0");
        conn.setRequestProperty("Accept", "application/json");
        
        int status = conn.getResponseCode();
        
        if (status == 200) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            String json = response.toString();
            
            // Extraer descripción 
            String descMarker = "\"lang\":\"en\",\"value\":\"";
            int descStart = json.indexOf(descMarker);
            String description = "No disponible";
            if (descStart >= 0) {
                descStart += descMarker.length();
                int descEnd = json.indexOf("\"}", descStart);
                if (descEnd >= 0) {
                    description = json.substring(descStart, descEnd);
                }
            }
            
            // Extraer severidad 
            String sevMarker = "\"baseSeverity\":\"";
            int sevStart = json.indexOf(sevMarker);
            String severity = "UNKNOWN";
            if (sevStart >= 0) {
                sevStart += sevMarker.length();
                int sevEnd = json.indexOf("\"", sevStart);
                if (sevEnd >= 0) {
                    severity = json.substring(sevStart, sevEnd);
                }
            }
            
            // Guardar como clave=valor
            String result = "descripcion=" + description + "\n" + "severidad=" + severity;
            Files.writeString(Path.of(OUTPUT_FILE), result);
            
            System.out.println("OK");
            System.exit(0);
        } else {
            Files.writeString(Path.of(OUTPUT_FILE), "error=" + status);
            System.err.println("ERROR HTTP: " + status);
            System.exit(1);
        }
    }
}