package cl.esfocar.backend_esfocar.util;

/**
 * Utilidades para normalización y manejo de RUT chileno.
 */
public final class RutUtils {

    private RutUtils() {
    }

    /**
     * Normaliza un RUT chileno eliminando puntos, espacios,
     * convirtiendo el dígito verificador a mayúscula ('k' -> 'K')
     * e insertando el guion antes del dígito verificador si no viene presente.
     *
     * Ejemplos:
     * - " 12.345.678-k " -> "12345678-K"
     * - "12.345.678-9"   -> "12345678-9"
     * - "12345678k"      -> "12345678-K"
     * - "9.876.543-k"    -> "9876543-K"
     * - "98765432"       -> "9876543-2"
     */
    public static String normalizarRut(String rut) {
        if (rut == null) {
            return null;
        }

        String limpio = rut.trim()
                .replace(".", "")
                .replace(" ", "")
                .toUpperCase();

        if (limpio.isEmpty()) {
            return limpio;
        }

        // Si no tiene guion y tiene al menos 2 caracteres (cuerpo + DV)
        if (!limpio.contains("-") && limpio.length() >= 2) {
            int len = limpio.length();
            limpio = limpio.substring(0, len - 1) + "-" + limpio.substring(len - 1);
        }

        return limpio;
    }

    /**
     * Obtiene solo los dígitos y el dígito verificador sin guion, puntos ni espacios en mayúscula.
     * Ejemplo: "12.345.678-K" -> "12345678K"
     */
    public static String limpiarSinPuntosNiGuion(String rut) {
        if (rut == null) {
            return null;
        }
        return rut.trim()
                .replace(".", "")
                .replace(" ", "")
                .replace("-", "")
                .toUpperCase();
    }
}
