package com.alura.literAlura.service.conversor;

public interface IConversor {
    <T> T obtenerDatos(String json, Class<T> clase);
}
