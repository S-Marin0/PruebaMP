package com.eventmaster.model.pattern.proxy;

import com.eventmaster.model.entity.Usuario;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// El objeto real que el Proxy representará
public class RecursoReal implements RecursoMultimedia {
    private String urlRecurso;
    private String tipo; // "imagen", "video"
    private byte[] contenidoCache; // Simulación de contenido, podría ser null y cargarse bajo demanda
    private long tamanoBytes;

    public RecursoReal(String urlRecurso, String tipo, long tamanoBytesEstimado) {
        this.urlRecurso = urlRecurso;
        this.tipo = tipo;
        this.tamanoBytes = tamanoBytesEstimado; // Podría calcularse al cargar por primera vez
        System.out.println("RecursoReal: Creado para " + urlRecurso + " (Tipo: " + tipo + ")");
        // En un escenario real, la carga del contenido se haría aquí o en mostrar() si no está cacheado.
        // this.contenidoCache = cargarContenidoDesdeUrl(urlRecurso); // Ejemplo de carga inmediata
    }

    public RecursoReal(String urlRecurso, String tipo) {
        this(urlRecurso, tipo, 0); // Tamaño desconocido inicialmente
    }


    private byte[] cargarContenidoDesdeUrl(String urlString) throws Exception {
        System.out.println("RecursoReal: Cargando contenido desde " + urlString + "...");
        // Simulación de descarga de contenido. En un caso real, esto implicaría E/S de red.
        // Aquí solo devolvemos un placeholder.
        if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Simulación simple, no maneja todos los casos de error o tipos de contenido
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    byte[] downloadedData = buffer.toByteArray();
                    this.tamanoBytes = downloadedData.length; // Actualizar tamaño real
                    System.out.println("RecursoReal: Contenido cargado (" + this.tamanoBytes + " bytes).");
                    return downloadedData;
                }
            } else {
                throw new Exception("RecursoReal: Error al cargar URL, código HTTP: " + connection.getResponseCode());
            }
        } else {
            // Simulación para URLs no HTTP (ej. archivos locales o placeholders)
            String simulatedContent = "Contenido simulado para: " + urlString + " (Tipo: " + tipo + ")";
             byte[] simulatedBytes = simulatedContent.getBytes(StandardCharsets.UTF_8);
            this.tamanoBytes = simulatedBytes.length;
            System.out.println("RecursoReal: Contenido simulado cargado (" + this.tamanoBytes + " bytes).");
            return simulatedBytes;
        }
    }

    @Override
    public String getUrlRecurso() {
        return urlRecurso;
    }

    @Override
    public byte[] mostrar(Usuario usuario) throws Exception {
        // En RecursoReal, asumimos que si se llama a mostrar(), el acceso ya fue validado por el Proxy
        // o no hay control de acceso a este nivel.
        System.out.println("RecursoReal: Mostrando contenido de " + urlRecurso + " para usuario " + (usuario != null ? usuario.getNombre() : "anónimo"));
        if (this.contenidoCache == null) {
            this.contenidoCache = cargarContenidoDesdeUrl(this.urlRecurso);
        }
        return this.contenidoCache;
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    @Override
    public long getTamanoBytes() {
        if (tamanoBytes == 0 && contenidoCache != null) {
            tamanoBytes = contenidoCache.length; // Si se cargó y no se sabía el tamaño
        }
        return tamanoBytes;
    }
}
