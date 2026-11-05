package com.mycompany.proyecto_desarrollo_app_web.service;

import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;

/** Lectura CSV con comas, comillas dobles y escape de comillas. Sin servicios externos. */
public final class ArchivoCsv {
    private ArchivoCsv() { }
    public static List<List<String>> leer(byte[] bytes) {
        String texto;
        try {
            texto = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("El archivo debe estar codificado en UTF-8.");
        }
        if (texto.startsWith("\uFEFF")) texto = texto.substring(1);
        List<List<String>> filas = new ArrayList<>();
        List<String> fila = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean comillas = false, cerrado = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (comillas) {
                if (c == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') { campo.append('"'); i++; }
                    else { comillas = false; cerrado = true; }
                } else campo.append(c);
            } else if (c == ',' || c == '\n' || c == '\r') {
                fila.add(campo.toString().trim()); campo.setLength(0); cerrado = false;
                if (c != ',') {
                    if (!(fila.size() == 1 && fila.getFirst().isEmpty())) filas.add(fila);
                    fila = new ArrayList<>();
                    if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') i++;
                }
            } else if (c == '"' && campo.isEmpty() && !cerrado) comillas = true;
            else {
                if (cerrado || c == '"') throw new IllegalArgumentException("CSV inválido: revise las comillas y separadores.");
                campo.append(c);
            }
        }
        if (comillas) throw new IllegalArgumentException("CSV inválido: hay comillas sin cerrar.");
        if (!fila.isEmpty() || !campo.isEmpty() || cerrado) { fila.add(campo.toString().trim()); filas.add(fila); }
        if (filas.size() < 2) throw new IllegalArgumentException("El CSV debe contener encabezados y al menos un registro.");
        if (filas.size() > 10001) throw new IllegalArgumentException("El máximo es de 10000 registros por archivo.");
        return filas;
    }
}
